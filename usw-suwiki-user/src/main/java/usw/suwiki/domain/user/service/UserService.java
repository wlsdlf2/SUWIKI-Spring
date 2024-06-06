package usw.suwiki.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.auth.token.RefreshToken;
import usw.suwiki.auth.token.service.ConfirmationTokenService;
import usw.suwiki.auth.token.service.RefreshTokenService;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.core.mail.EmailSender;
import usw.suwiki.core.secure.Encoder;
import usw.suwiki.core.secure.TokenAgent;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.UserRepository;
import usw.suwiki.domain.user.dto.UserResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static usw.suwiki.core.exception.ExceptionCode.DUPLICATED_ID_OR_EMAIL;
import static usw.suwiki.core.exception.ExceptionCode.INVALID_EMAIL_FORMAT;
import static usw.suwiki.core.exception.ExceptionCode.LOGIN_FAIL;
import static usw.suwiki.core.exception.ExceptionCode.USER_NOT_FOUND;
import static usw.suwiki.core.mail.MailType.EMAIL_AUTH;
import static usw.suwiki.core.mail.MailType.FIND_ID;
import static usw.suwiki.core.mail.MailType.FIND_PASSWORD;
import static usw.suwiki.domain.user.dto.UserResponse.BlackedReason;
import static usw.suwiki.domain.user.dto.UserResponse.MyPage;
import static usw.suwiki.domain.user.dto.UserResponse.RestrictedReason;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
  private static final Predicate<String> IS_NOT_USW_EMAIL = email -> !email.contains("@suwon.ac.kr");

  private final Encoder encoder;
  private final EmailSender emailSender;

  private final UserRepository userRepository;

  private final BlacklistService blacklistService;
  private final UserIsolationService userIsolationService;

  private final RestrictService restrictService;
  private final FavoriteMajorService favoriteMajorService;

  private final RefreshTokenService refreshTokenService;
  private final ConfirmationTokenService confirmationTokenService;

  private final TokenAgent tokenAgent;

  @Transactional(readOnly = true)
  public List<User> loadUsersLastLoginBetween(LocalDateTime startTime, LocalDateTime endTime) {
    return userRepository.findByLastLoginBetween(startTime, endTime);
  }

  @Transactional(readOnly = true)
  public List<String> loadAllFavoriteMajors(Long userId) {
    return favoriteMajorService.findMajorTypeByUser(userId);
  }

  @Transactional(readOnly = true)
  public UserResponse.MyPage loadMyPage(Long userId) {
    User user = loadById(userId);

    return new MyPage(
      user.getLoginId(),
      user.getEmail(),
      user.getPoint(),
      user.getWrittenEvaluation(),
      user.getWrittenExam(),
      user.getViewExamCount()
    );
  }

  @Transactional(readOnly = true)
  public List<BlackedReason> loadBlackListReason(Long id) {
    var user = loadById(id);
    return blacklistService.loadAllBlacklistLogs(user.getId());
  }

  @Transactional(readOnly = true)
  public List<RestrictedReason> loadRestrictedReason(Long userId) {
    var user = loadById(userId);
    return restrictService.loadRestrictedLog(user.getId());
  }

  public long countAllUsers() {
    return userRepository.count() + userIsolationService.countAllIsolatedUsers();
  }

  public boolean isDuplicatedId(String loginId) {
    return userRepository.existsByLoginId(loginId) || userIsolationService.isIsolatedByLoginId(loginId);
  }

  public boolean isDuplicatedEmail(String email) {
    return userRepository.existsByEmail(email) || userIsolationService.isIsolatedByEmail(email);
  }

  public User loadByLoginId(String loginId) {
    return userRepository.findByLoginId(loginId)
      .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
  }

  public void join(String loginId, String password, String email) {
    blacklistService.validateNotBlack(email);

    if (isDuplicatedId(loginId) || isDuplicatedEmail(email)) {
      throw new AccountException(DUPLICATED_ID_OR_EMAIL);
    }

    if (IS_NOT_USW_EMAIL.test(email)) {
      throw new AccountException(INVALID_EMAIL_FORMAT);
    }

    var user = userRepository.save(User.join(loginId, encoder.encode(password), email));

    emailSender.send(email, EMAIL_AUTH, confirmationTokenService.requestConfirm(user.getId()));
  }

  public Map<String, String> login(String loginId, String password) {
    userIsolationService.wakeIfSleeping(loginId, encoder, password);

    var user = userRepository.findByLoginId(loginId).orElseThrow(() -> new AccountException(LOGIN_FAIL));

    user.validateLoginable(encoder, password);

    confirmationTokenService.validateEmailAuthorized(user.getId());

    return generateJwt(user);
  }

  // legacy
  private Map<String, String> generateJwt(User user) {
    return new HashMap<>() {{
      put("AccessToken", tokenAgent.createAccessToken(user.getId(), user.toClaim()));
      put("RefreshToken", tokenAgent.login(user.getId()));
    }};
  }

  public void findId(String email) {
    userIsolationService.wakeIfSleeping(email);

    var user = userRepository.findByEmail(email)
      .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

    emailSender.send(email, FIND_ID, user.getLoginId());
  }

  public void findPw(String loginId, String email) {
    userIsolationService.wakeIfSleeping(loginId, email);

    var user = userRepository.findByLoginIdAndEmail(loginId, email)
      .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

    emailSender.send(email, FIND_PASSWORD, user.resetPassword(encoder));
  }

  public void rewardReport(Long id) {
    var user = loadById(id);
    user.report();
  }

  public void evaluate(Long userId) {
    var user = loadById(userId);
    user.evaluate();
  }

  public void eraseEvaluation(Long userId) {
    var user = loadById(userId);
    user.eraseEvaluation();
  }

  public void writeExamPost(Long userId) {
    var user = loadById(userId);
    user.writeExamPost();
  }

  public void purchaseExamPost(Long userId) {
    var user = loadById(userId);
    user.purchaseExamPost();
  }

  public void eraseExamPost(Long userId) {
    var user = loadById(userId);
    user.eraseExamPost();
  }

  public void changePassword(Long userId, String prePassword, String newPassword) {
    var user = loadById(userId);
    user.changePassword(encoder, prePassword, newPassword);
  }

  public Map<String, String> reissue(String payload) {
    RefreshToken refreshToken = refreshTokenService.loadByPayload(payload);
    User user = loadById(refreshToken.getUserIdx());

    return new HashMap<>() {{
      put("AccessToken", tokenAgent.createAccessToken(user.getId(), user.toClaim()));
      put("RefreshToken", tokenAgent.reissue(refreshToken.getPayload()));
    }};
  }

  public void quit(Long userId, String inputPassword) {
    var user = loadById(userId);
    user.validatePassword(encoder, inputPassword);
    user.waitQuit();
  }

  public void restrict(Long reportedId, long banPeriod, String reason, String judgement) {
    var user = loadById(reportedId);
    user.reported();

    if (user.isArrestable()) {
      blacklistService.overRestricted(user.getId(), user.getEmail());
      return;
    }

    restrictService.restrict(reportedId, banPeriod, reason, judgement);
  }

  public void release(Long reportedId) {
    var user = loadById(reportedId);
    user.release();
    restrictService.release(reportedId);
  }

  public void black(Long reportedId, String reason, String judgement) {
    var user = loadById(reportedId);
    user.reported();

    blacklistService.black(reportedId, user.getEmail(), reason, judgement);
  }

  public void saveFavoriteMajor(Long userId, String majorType) {
    favoriteMajorService.save(userId, majorType);
  }

  public void deleteFavoriteMajor(Long userId, String majorType) {
    favoriteMajorService.delete(userId, majorType);
  }

  public void deleteById(Long userId) {
    userRepository.deleteById(userId);
  }

  public void deleteAllInBatch(List<Long> userIds) {
    userRepository.deleteAllByIdInBatch(userIds);
  }

  private User loadById(Long userId) {
    return userRepository.findById(userId)
      .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
  }
}

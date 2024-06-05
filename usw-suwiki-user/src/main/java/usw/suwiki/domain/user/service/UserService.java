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
import java.util.Optional;

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
  private static final String USW_EMAIL_FORMAT = "@suwon.ac.kr";

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

  public boolean isDuplicatedId(String loginId) {
    return userRepository.existsByLoginId(loginId) || userIsolationService.isIsolatedByLoginId(loginId);
  }

  public boolean isDuplicatedEmail(String email) {
    return userRepository.existsByEmail(email) || userIsolationService.isIsolatedByEmail(email);
  }

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
    User user = loadUserById(userId);

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
    var user = loadUserById(id);
    return blacklistService.loadAllBlacklistLogs(user.getId());
  }

  @Transactional(readOnly = true)
  public List<RestrictedReason> loadRestrictedReason(Long userId) {
    var user = loadUserById(userId);
    return restrictService.loadRestrictedLog(user.getId());
  }

  public long countAllUsers() {
    return userRepository.count() + userIsolationService.countAllIsolatedUsers();
  }

  public User loadByLoginId(String loginId) {
    return userRepository.findByLoginId(loginId)
      .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
  }

  public User loadUserById(Long userId) {
    return userRepository.findById(userId)
      .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
  }

  public void join(String loginId, String password, String email) {
    blacklistService.validateNotBlack(email);

    if (isDuplicatedId(loginId) || isDuplicatedEmail(email)) {
      throw new AccountException(DUPLICATED_ID_OR_EMAIL);
    }

    if (!email.contains(USW_EMAIL_FORMAT)) {
      throw new AccountException(INVALID_EMAIL_FORMAT);
    }

    var user = User.join(loginId, encoder.encode(password), email);
    userRepository.save(user);

    emailSender.send(email, EMAIL_AUTH, confirmationTokenService.requestConfirm(user.getId()));
  }

  public void findId(String email) {
    Optional<User> requestUser = userRepository.findByEmail(email);
    Optional<String> isolatedLoginId = userIsolationService.findIsolatedLoginIdByEmail(email);

    if (requestUser.isPresent()) {
      emailSender.send(email, FIND_ID, requestUser.get().getLoginId());
      return;
    } else if (isolatedLoginId.isPresent()) {
      emailSender.send(email, FIND_ID, isolatedLoginId.get());
      return;
    }

    throw new AccountException(USER_NOT_FOUND);
  }

  // todo: isoloation user table부터 확인 후 user table 확인하도록 수정
  public void findPw(String loginId, String email) {
    Optional<User> userByLoginId = userRepository.findByLoginId(loginId);

    if (userByLoginId.isEmpty()) {
      throw new AccountException(USER_NOT_FOUND);
    }

    Optional<User> userByEmail = userRepository.findByEmail(email);
    if (userByEmail.isEmpty()) {
      throw new AccountException(USER_NOT_FOUND);
    }

    if (userByLoginId.equals(userByEmail)) {
      User user = userByLoginId.get();
      emailSender.send(email, FIND_PASSWORD, user.resetPassword(encoder));
      return;
    } else if (userIsolationService.isSleeping(loginId, email)) {
      String newPassword = userIsolationService.updateIsolatedUserPassword(encoder, email);
      emailSender.send(email, FIND_PASSWORD, newPassword);
      return;
    }

    throw new AccountException(USER_NOT_FOUND);
  }

  public Map<String, String> login(String loginId, String inputPassword) {
    if (userRepository.existsByLoginId(loginId)) {
      User user = loadByLoginId(loginId);

      confirmationTokenService.validateEmailAuthorized(user.getId());

      if (user.isPasswordEquals(encoder, inputPassword)) {
        user.login();
        return generateJwt(user);
      }
    } else if (userIsolationService.isLoginable(loginId, inputPassword, encoder)) {
      User user = userIsolationService.wake(this, loginId);
      return generateJwt(user);
    }

    throw new AccountException(LOGIN_FAIL);
  }

  // legacy
  private Map<String, String> generateJwt(User user) {
    return new HashMap<>() {{
      put("AccessToken", tokenAgent.createAccessToken(user.getId(), user.toClaim()));
      put("RefreshToken", tokenAgent.login(user.getId()));
    }};
  }

  public void rewardReport(Long id) {
    var user = loadUserById(id);
    user.report();
  }

  public void evaluate(Long userId) {
    var user = loadUserById(userId);
    user.evaluate();
  }

  public void eraseEvaluation(Long userId) {
    var user = loadUserById(userId);
    user.eraseEvaluation();
  }

  public void writeExamPost(Long userId) {
    var user = loadUserById(userId);
    user.writeExamPost();
  }

  public void purchaseExamPost(Long userId) {
    var user = loadUserById(userId);
    user.purchaseExamPost();
  }

  public void eraseExamPost(Long userId) {
    var user = loadUserById(userId);
    user.eraseExamPost();
  }

  public void sleep(Long userIdx) {
    User user = loadUserById(userIdx);
    user.sleep();
  }

  public void changePassword(Long userId, String prePassword, String newPassword) {
    var user = loadUserById(userId);
    user.changePassword(encoder, prePassword, newPassword);
  }

  public Map<String, String> reissue(String payload) {
    RefreshToken refreshToken = refreshTokenService.loadByPayload(payload);
    User user = loadUserById(refreshToken.getUserIdx());

    return new HashMap<>() {{
      put("AccessToken", tokenAgent.createAccessToken(user.getId(), user.toClaim()));
      put("RefreshToken", tokenAgent.reissue(refreshToken.getPayload()));
    }};
  }

  public void quit(Long userId, String inputPassword) {
    var user = loadUserById(userId);
    user.validatePassword(encoder, inputPassword);
    user.waitQuit();
  }

  public void restrict(Long reportedId, long banPeriod, String reason, String judgement) {
    var user = loadUserById(reportedId);
    user.reported();

    if (user.isArrestable()) {
      blacklistService.overRestricted(user.getId(), user.getEmail());
      return;
    }

    restrictService.restrict(reportedId, banPeriod, reason, judgement);
  }

  public void black(Long userId, String reason, String judgement) {
    var user = loadUserById(userId);
    user.reported();

    blacklistService.black(userId, user.getEmail(), reason, judgement);
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
}

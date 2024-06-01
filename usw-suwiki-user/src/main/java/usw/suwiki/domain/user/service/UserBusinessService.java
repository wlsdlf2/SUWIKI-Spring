package usw.suwiki.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.auth.token.RefreshToken;
import usw.suwiki.auth.token.service.ConfirmationTokenCRUDService;
import usw.suwiki.auth.token.service.RefreshTokenService;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.core.mail.EmailSender;
import usw.suwiki.core.secure.PasswordEncoder;
import usw.suwiki.core.secure.TokenAgent;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.dto.UserResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static usw.suwiki.common.response.ApiResponseFactory.overlapFalseFlag;
import static usw.suwiki.common.response.ApiResponseFactory.overlapTrueFlag;
import static usw.suwiki.common.response.ApiResponseFactory.successFlag;
import static usw.suwiki.core.exception.ExceptionType.EMAIL_NOT_AUTHED;
import static usw.suwiki.core.exception.ExceptionType.INVALID_EMAIL_FORMAT;
import static usw.suwiki.core.exception.ExceptionType.LOGIN_ID_OR_EMAIL_OVERLAP;
import static usw.suwiki.core.exception.ExceptionType.PASSWORD_ERROR;
import static usw.suwiki.core.exception.ExceptionType.USER_NOT_FOUND;
import static usw.suwiki.core.exception.ExceptionType.USER_NOT_FOUND_BY_EMAIL;
import static usw.suwiki.core.exception.ExceptionType.USER_NOT_FOUND_BY_LOGINID;
import static usw.suwiki.core.mail.MailType.EMAIL_AUTH;
import static usw.suwiki.core.mail.MailType.FIND_ID;
import static usw.suwiki.core.mail.MailType.FIND_PASSWORD;
import static usw.suwiki.domain.user.dto.UserResponse.BlackedReason;
import static usw.suwiki.domain.user.dto.UserResponse.MyPage;
import static usw.suwiki.domain.user.dto.UserResponse.RestrictedReason;

@Service
@Transactional
@RequiredArgsConstructor
public class UserBusinessService {
  private static final String MAIL_FORMAT = "@suwon.ac.kr";

  private final EmailSender emailSender;
  private final PasswordEncoder passwordEncoder;

  private final UserCRUDService userCRUDService;
  private final BlacklistDomainService blacklistDomainService;
  private final UserIsolationCRUDService userIsolationCRUDService;
  private final BlacklistDomainCRUDService blacklistDomainCRUDService;
  private final RestrictingUserCRUDService restrictingUserCRUDService;

  private final FavoriteMajorService favoriteMajorService;

  private final CleanReportService cleanReportService;
  private final CleanViewExamService cleanViewExamService;
  private final CleanExamPostsService cleanExamPostsService;
  private final CleanEvaluatePostsService cleanEvaluatePostsService;

  private final RefreshTokenService refreshTokenService;
  private final ConfirmationTokenCRUDService confirmationTokenCRUDService;

  private final TokenAgent tokenAgent;

  @Transactional(readOnly = true)
  public Map<String, Boolean> isDuplicatedId(String loginId) {
    if (userCRUDService.findOptionalByLoginId(loginId).isPresent() || userIsolationCRUDService.isIsolatedByLoginId(loginId)) {
      return overlapTrueFlag();
    }
    return overlapFalseFlag();
  }

  @Transactional(readOnly = true)
  public Map<String, Boolean> isDuplicatedEmail(String email) {
    if (userCRUDService.findOptionalByEmail(email).isPresent() || userIsolationCRUDService.isIsolatedByEmail(email)) {
      return overlapTrueFlag();
    }
    return overlapFalseFlag();
  }

  public void wroteEvaluation(Long userId) {
    User user = userCRUDService.loadUserById(userId);
    user.writeEvaluatePost();
  }

  public void eraseEvaluation(Long userId) {
    User user = userCRUDService.loadUserById(userId);
    user.deleteEvaluatePost();
  }

  public void writeExamPost(Long userId) {
    var user = userCRUDService.loadUserById(userId);
    user.writeExamPost();
  }

  public void purchaseExamPost(Long userId) {
    var user = userCRUDService.loadUserById(userId);
    user.purchaseExamPost();
  }

  public void eraseExamPost(Long userId) {
    var user = userCRUDService.loadUserById(userId);
    user.eraseExamPost();
  }

  public Map<String, Boolean> join(String loginId, String password, String email) {
    blacklistDomainService.isUserInBlackListThatRequestJoin(email);

    if (userCRUDService.findOptionalByLoginId(loginId).isPresent() ||
        userIsolationCRUDService.isIsolatedByLoginId(loginId) ||
        userCRUDService.findOptionalByEmail(email).isPresent() ||
        userIsolationCRUDService.isIsolatedByEmail(email)
    ) {
      throw new AccountException(LOGIN_ID_OR_EMAIL_OVERLAP);
    }

    if (!email.contains(MAIL_FORMAT)) {
      throw new AccountException(INVALID_EMAIL_FORMAT);
    }

    User user = User.init(loginId, passwordEncoder.encode(password), email);
    userCRUDService.saveUser(user);

    emailSender.send(email, EMAIL_AUTH, confirmationTokenCRUDService.save(user.getId()));
    return successFlag();
  }

  public Map<String, Boolean> findId(String email) {
    Optional<User> requestUser = userCRUDService.findOptionalByEmail(email);
    Optional<String> isolatedLoginId = userIsolationCRUDService.getIsolatedLoginIdByEmail(email);

    if (requestUser.isPresent()) {
      emailSender.send(email, FIND_ID, requestUser.get().getLoginId());
      return successFlag();
    } else if (isolatedLoginId.isPresent()) {
      emailSender.send(email, FIND_ID, isolatedLoginId.get());
      return successFlag();
    }
    throw new AccountException(USER_NOT_FOUND);
  }

  // todo: isoloation user table부터 확인 후 user table 확인하도록 수정
  public Map<String, Boolean> findPw(String loginId, String email) {
    Optional<User> userByLoginId = userCRUDService.findOptionalByLoginId(loginId);

    if (userByLoginId.isEmpty()) {
      throw new AccountException(USER_NOT_FOUND_BY_LOGINID);
    }

    Optional<User> userByEmail = userCRUDService.findOptionalByEmail(email);
    if (userByEmail.isEmpty()) {
      throw new AccountException(USER_NOT_FOUND_BY_EMAIL);
    }

    if (userByLoginId.equals(userByEmail)) {
      User user = userByLoginId.get();
      emailSender.send(email, FIND_PASSWORD, user.resetPassword(passwordEncoder));
      return successFlag();
    } else if (userIsolationCRUDService.isRetrievedUserEquals(email, loginId)) {
      String newPassword = userIsolationCRUDService.updateIsolatedUserPassword(passwordEncoder, email);
      emailSender.send(email, FIND_PASSWORD, newPassword);
      return successFlag();
    }
    throw new AccountException(USER_NOT_FOUND_BY_EMAIL);
  }

  public Map<String, String> login(String loginId, String inputPassword) {
    if (userCRUDService.findOptionalByLoginId(loginId).isPresent()) {
      User user = userCRUDService.loadByLoginId(loginId);

      var optionalConfirmationToken = confirmationTokenCRUDService.findOptionalTokenByUserId(user.getId());

      if (optionalConfirmationToken.isEmpty()) {
        throw new AccountException(EMAIL_NOT_AUTHED);
      }

      if (!optionalConfirmationToken.get().isVerified()) {
        throw new AccountException(EMAIL_NOT_AUTHED);
      }

      if (user.isPasswordEquals(passwordEncoder, inputPassword)) {
        user.login();
        return generateJwt(user);
      }
    } else if (userIsolationCRUDService.isLoginableIsolatedUser(loginId, inputPassword, passwordEncoder)) {
      User user = userIsolationCRUDService.wakeIsolated(userCRUDService, loginId);
      return generateJwt(user);
    }

    throw new AccountException(PASSWORD_ERROR);
  }


  public Map<String, Boolean> editPassword(Long userId, String prePassword, String newPassword) {
    var user = userCRUDService.loadUserById(userId);
    user.changePassword(passwordEncoder, prePassword, newPassword);
    return successFlag();
  }

  public UserResponse.MyPage loadMyPage(Long userId) {
    User user = userCRUDService.loadUserById(userId);
    return MyPage.from(user);
  }

  public Map<String, String> reissue(String payload) {
    RefreshToken refreshToken = refreshTokenService.loadByPayload(payload);
    User user = userCRUDService.loadUserById(refreshToken.getUserIdx());
    return reissueJwt(user, refreshToken.getPayload());
  }

  public Map<String, Boolean> quit(Long userId, String inputPassword) {
    var user = userCRUDService.loadUserById(userId);
    user.validatePassword(passwordEncoder, inputPassword);

    favoriteMajorService.clean(user.getId());
    cleanReportService.clean(user.getId());
    cleanViewExamService.clean(user.getId());
    cleanExamPostsService.clean(user.getId());
    cleanEvaluatePostsService.clean(user.getId());

    user.waitQuit();
    return successFlag();
  }

  public List<BlackedReason> loadBlackListReason(Long id) {
    var user = userCRUDService.loadUserById(id);
    return blacklistDomainCRUDService.loadAllBlacklistLogs(user.getId());
  }

  public List<RestrictedReason> loadRestrictedReason(Long userId) {
    var user = userCRUDService.loadUserById(userId);
    return restrictingUserCRUDService.loadRestrictedLog(user.getId());
  }

  public void saveFavoriteMajor(Long userId, String majorType) {
    favoriteMajorService.save(userId, majorType);
  }

  public void deleteFavoriteMajor(Long userId, String majorType) {
    favoriteMajorService.delete(userId, majorType);
  }

  public List<String> loadAllFavoriteMajors(Long userId) {
    return favoriteMajorService.findMajorTypeByUser(userId);
  }

  private void rollBackUserFromSleeping(Long userIdx, String loginId, String password, String email) {
    User user = userCRUDService.loadUserById(userIdx);
    user.wake(loginId, password, email);
  }

  private Map<String, String> generateJwt(User user) {
    return new HashMap<>() {{
      put("AccessToken", tokenAgent.createAccessToken(user.getId(), user.toClaim()));
      put("RefreshToken", tokenAgent.login(user.getId()));
    }};
  }

  private Map<String, String> reissueJwt(User user, String refreshTokenPayload) {
    return new HashMap<>() {{
      put("AccessToken", tokenAgent.createAccessToken(user.getId(), user.toClaim()));
      put("RefreshToken", tokenAgent.reissue(refreshTokenPayload));
    }};
  }
}

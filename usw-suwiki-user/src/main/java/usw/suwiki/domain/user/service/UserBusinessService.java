package usw.suwiki.domain.user.service;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.auth.token.ConfirmationToken;
import usw.suwiki.auth.token.RefreshToken;
import usw.suwiki.auth.token.service.ConfirmationTokenCRUDService;
import usw.suwiki.auth.token.service.RefreshTokenService;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.core.mail.EmailSender;
import usw.suwiki.core.secure.PasswordEncoder;
import usw.suwiki.core.secure.TokenAgent;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.dto.MajorRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static usw.suwiki.common.response.ApiResponseFactory.overlapFalseFlag;
import static usw.suwiki.common.response.ApiResponseFactory.overlapTrueFlag;
import static usw.suwiki.common.response.ApiResponseFactory.successFlag;
import static usw.suwiki.core.exception.ExceptionType.EMAIL_NOT_AUTHED;
import static usw.suwiki.core.exception.ExceptionType.IS_NOT_EMAIL_FORM;
import static usw.suwiki.core.exception.ExceptionType.LOGIN_ID_OR_EMAIL_OVERLAP;
import static usw.suwiki.core.exception.ExceptionType.PASSWORD_ERROR;
import static usw.suwiki.core.exception.ExceptionType.USER_NOT_EXISTS;
import static usw.suwiki.core.exception.ExceptionType.USER_NOT_FOUND_BY_EMAIL;
import static usw.suwiki.core.exception.ExceptionType.USER_NOT_FOUND_BY_LOGINID;
import static usw.suwiki.core.mail.MailType.EMAIL_AUTH;
import static usw.suwiki.core.mail.MailType.FIND_ID;
import static usw.suwiki.core.mail.MailType.FIND_PASSWORD;
import static usw.suwiki.domain.user.dto.UserResponse.LoadMyBlackListReasonResponse;
import static usw.suwiki.domain.user.dto.UserResponse.LoadMyRestrictedReasonResponse;
import static usw.suwiki.domain.user.dto.UserResponse.UserInformationResponse;

@Service
@Transactional
@RequiredArgsConstructor
public class UserBusinessService {
  private static final String MAIL_FORM = "@suwon.ac.kr";

  private final EmailSender emailSender;
  private final PasswordEncoder passwordEncoder;

  private final UserCRUDService userCRUDService;
  private final BlacklistDomainService blacklistDomainService;
  private final UserIsolationCRUDService userIsolationCRUDService;
  private final BlacklistDomainCRUDService blacklistDomainCRUDService;
  private final RestrictingUserCRUDService restrictingUserCRUDService;

  private final FavoriteMajorService favoriteMajorService;

  private final ClearReportService clearReportService;
  private final ClearViewExamService clearViewExamService;
  private final ClearExamPostsService clearExamPostsService;
  private final ClearEvaluatePostsService clearEvaluatePostsService;

  private final RefreshTokenService refreshTokenService;
  private final ConfirmationTokenCRUDService confirmationTokenCRUDService;

  private final TokenAgent tokenAgent;

  @Transactional(readOnly = true)
  public Map<String, Boolean> isDuplicatedId(String loginId) {
    if (userCRUDService.loadWrappedUserFromLoginId(loginId).isPresent() ||
        userIsolationCRUDService.isIsolatedByLoginId(loginId)
    ) {
      return overlapTrueFlag();
    }
    return overlapFalseFlag();
  }

  @Transactional(readOnly = true)
  public Map<String, Boolean> isDuplicatedEmail(String email) {
    if (userCRUDService.loadWrappedUserFromEmail(email).isPresent() ||
        userIsolationCRUDService.isIsolatedByEmail(email)
    ) {
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

    if (userCRUDService.loadWrappedUserFromLoginId(loginId).isPresent() ||
        userIsolationCRUDService.isIsolatedByLoginId(loginId) ||
        userCRUDService.loadWrappedUserFromEmail(email).isPresent() ||
        userIsolationCRUDService.isIsolatedByEmail(email)
    ) {
      throw new AccountException(LOGIN_ID_OR_EMAIL_OVERLAP);
    }

    if (!email.contains(MAIL_FORM)) {
      throw new AccountException(IS_NOT_EMAIL_FORM);
    }

    User user = User.init(loginId, passwordEncoder.encode(password), email);
    userCRUDService.saveUser(user);

    ConfirmationToken confirmationToken = new ConfirmationToken(user.getId());
    confirmationTokenCRUDService.saveConfirmationToken(confirmationToken);

    emailSender.send(email, EMAIL_AUTH, confirmationToken.getToken());
    return successFlag();
  }

  public Map<String, Boolean> findId(String email) {
    Optional<User> requestUser = userCRUDService.loadWrappedUserFromEmail(email);
    Optional<String> isolatedLoginId = userIsolationCRUDService.getIsolatedLoginIdByEmail(email);

    if (requestUser.isPresent()) {
      emailSender.send(email, FIND_ID, requestUser.get().getLoginId());
      return successFlag();
    } else if (isolatedLoginId.isPresent()) {
      emailSender.send(email, FIND_ID, isolatedLoginId.get());
      return successFlag();
    }
    throw new AccountException(USER_NOT_EXISTS);
  }

  // todo: isoloation user table부터 확인 후 user table 확인하도록 수정
  public Map<String, Boolean> findPw(String loginId, String email) {
    Optional<User> userByLoginId = userCRUDService.loadWrappedUserFromLoginId(loginId);

    if (userByLoginId.isEmpty()) {
      throw new AccountException(USER_NOT_FOUND_BY_LOGINID);
    }

    Optional<User> userByEmail = userCRUDService.loadWrappedUserFromEmail(email);
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
    if (userCRUDService.loadWrappedUserFromLoginId(loginId).isPresent()) {
      User user = userCRUDService.loadUserFromLoginId(loginId);

      var optionalConfirmationToken = confirmationTokenCRUDService.loadConfirmationTokenFromUserIdx(user.getId());

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
    }

    if (userIsolationCRUDService.isLoginableIsolatedUser(loginId, inputPassword, passwordEncoder)) {
      User user = userIsolationCRUDService.wakeIsolated(userCRUDService, loginId);
      return generateJwt(user);
    }

    throw new AccountException(PASSWORD_ERROR);
  }


  public Map<String, Boolean> editPassword(Long userId, String prePassword, String newPassword) {
    var user = userCRUDService.loadUserFromUserIdx(userId);
    user.changePassword(passwordEncoder, prePassword, newPassword);
    return successFlag();
  }

  public UserInformationResponse loadMyPage(Long userId) {
    User user = userCRUDService.loadUserFromUserIdx(userId);
    return UserInformationResponse.toMyPageResponse(user);
  }

  public Map<String, String> reissueWeb(Cookie requestRefreshCookie) {
    String payload = requestRefreshCookie.getValue();
    RefreshToken refreshToken = refreshTokenService.loadByPayload(payload);
    User user = userCRUDService.loadUserFromUserIdx(refreshToken.getUserIdx());
    return refreshJwt(user, payload);
  }

  public Map<String, String> reissueMobile(String payload) {
    RefreshToken refreshToken = refreshTokenService.loadByPayload(payload);
    User user = userCRUDService.loadUserFromUserIdx(refreshToken.getUserIdx());
    return refreshJwt(user, refreshToken.getPayload());
  }

  public Map<String, Boolean> quit(Long userId, String inputPassword) {
    User user = userCRUDService.loadUserFromUserIdx(userId);

    if (!user.isPasswordEquals(passwordEncoder, inputPassword)) {
      throw new AccountException(PASSWORD_ERROR);
    }

    favoriteMajorService.clear(user.getId());
    clearReportService.clear(user.getId());
    clearViewExamService.clear(user.getId());
    clearExamPostsService.clear(user.getId());
    clearEvaluatePostsService.clear(user.getId());

    user.waitQuit();
    return successFlag();
  }

  public List<LoadMyBlackListReasonResponse> executeLoadBlackListReason(Long id) {
    User requestUser = userCRUDService.loadUserFromUserIdx(id);
    return blacklistDomainCRUDService.loadAllBlacklistLog(requestUser.getId());
  }

  public List<LoadMyRestrictedReasonResponse> executeLoadRestrictedReason(Long userId) {
    User requestUser = userCRUDService.loadUserFromUserIdx(userId);
    return restrictingUserCRUDService.loadRestrictedLog(requestUser.getId());
  }

  public void saveFavoriteMajor(Long userId, MajorRequest majorRequest) {
    favoriteMajorService.save(userId, majorRequest.getMajorType());
  }

  public void deleteFavoriteMajor(Long userId, String majorType) {
    favoriteMajorService.delete(userId, majorType);
  }

  public List<String> executeFavoriteMajorLoad(Long userId) {
    return favoriteMajorService.findMajorTypeByUser(userId);
  }

  private void rollBackUserFromSleeping(Long userIdx, String loginId, String password, String email) {
    User user = userCRUDService.loadUserFromUserIdx(userIdx);
    user.wake(loginId, password, email);
  }

  private Map<String, String> generateJwt(User user) {
    return new HashMap<>() {{
      put("AccessToken", tokenAgent.createAccessToken(user.getId(), user.toClaim()));
      put("RefreshToken", tokenAgent.login(user.getId()));
    }};
  }

  private Map<String, String> refreshJwt(User user, String refreshTokenPayload) {
    return new HashMap<>() {{
      put("AccessToken", tokenAgent.createAccessToken(user.getId(), user.toClaim()));
      put("RefreshToken", tokenAgent.reissue(refreshTokenPayload));
    }};
  }
}

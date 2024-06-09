package usw.suwiki.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.core.mail.EmailSender;
import usw.suwiki.core.secure.Encoder;
import usw.suwiki.core.secure.TokenGenerator;
import usw.suwiki.core.secure.model.Tokens;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.UserRepository;

import java.util.function.Predicate;

import static usw.suwiki.core.exception.ExceptionCode.DUPLICATED_ID_OR_EMAIL;
import static usw.suwiki.core.exception.ExceptionCode.INVALID_EMAIL_FORMAT;
import static usw.suwiki.core.exception.ExceptionCode.LOGIN_FAIL;
import static usw.suwiki.core.exception.ExceptionCode.USER_NOT_FOUND;
import static usw.suwiki.core.mail.MailType.EMAIL_AUTH;
import static usw.suwiki.core.mail.MailType.FIND_ID;
import static usw.suwiki.core.mail.MailType.FIND_PASSWORD;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
  private static final Predicate<String> IS_NOT_USW_EMAIL = email -> !email.contains("@suwon.ac.kr");

  private final UserRepository userRepository;
  private final BlacklistService blacklistService;
  private final UserIsolationService userIsolationService;

  private final ConfirmationTokenService confirmationTokenService;

  private final Encoder encoder;
  private final EmailSender emailSender;
  private final TokenGenerator tokenGenerator;

  public boolean isDuplicatedId(String loginId) {
    return userRepository.existsByLoginId(loginId) || userIsolationService.isIsolatedByLoginId(loginId);
  }

  public boolean isDuplicatedEmail(String email) {
    return userRepository.existsByEmail(email) || userIsolationService.isIsolatedByEmail(email);
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

  public Tokens login(String loginId, String password) {
    userIsolationService.wakeIfSleeping(loginId, encoder, password);

    var user = loadForLogin(loginId);

    user.validateLoginable(encoder, password);

    confirmationTokenService.validateEmailAuthorized(user.getId());

    return tokenGenerator.login(user.getId(), user.toClaim());
  }

  public Tokens reissue(String refreshToken) {
    return tokenGenerator.reissue(refreshToken);
  }

  public void findId(String email) {
    userIsolationService.wakeIfSleeping(email);

    var user = userRepository.findByEmail(email).orElseThrow(() -> new AccountException(USER_NOT_FOUND));

    emailSender.send(email, FIND_ID, user.getLoginId());
  }

  public void findPw(String loginId, String email) {
    userIsolationService.wakeIfSleeping(loginId, email);

    var user = userRepository.findByLoginIdAndEmail(loginId, email).orElseThrow(() -> new AccountException(USER_NOT_FOUND));

    emailSender.send(email, FIND_PASSWORD, user.resetPassword(encoder));
  }

  public User loadForLogin(String loginId) {
    return userRepository.findByLoginId(loginId)
      .orElseThrow(() -> new AccountException(LOGIN_FAIL));
  }
}

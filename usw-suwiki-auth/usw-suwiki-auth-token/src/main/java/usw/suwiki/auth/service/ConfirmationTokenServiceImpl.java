package usw.suwiki.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.auth.token.ConfirmationToken;
import usw.suwiki.auth.token.ConfirmationTokenRepository;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.domain.user.service.ConfirmationTokenService;
import usw.suwiki.domain.user.service.UserService;

import static usw.suwiki.core.exception.ExceptionCode.EMAIL_NOT_AUTHED;

@Service
@Transactional
@RequiredArgsConstructor
class ConfirmationTokenServiceImpl implements ConfirmationTokenService {
  private final ConfirmationTokenRepository confirmationTokenRepository;
  private final UserService userService;

  @Override
  public void validateEmailAuthorized(Long userId) {
    confirmationTokenRepository.findByUserIdx(userId)
      .orElseThrow(() -> new AccountException(EMAIL_NOT_AUTHED))
      .validateVerified();
  }

  @Override
  public String requestConfirm(Long userId) {
    var confirmationToken = confirmationTokenRepository.save(new ConfirmationToken(userId));
    return confirmationToken.getToken();
  }

  @Override
  public String confirm(String token) {
    return confirmationTokenRepository.findByToken(token)
      .map(it -> it.isExpired() ? expire(it) : confirm(it))
      .orElse(ConfirmMessage.ERROR.content);
  }

  private String confirm(ConfirmationToken token) {
    token.confirm();
    userService.activate(token.getUserIdx());
    return ConfirmMessage.SUCCESS.content;
  }

  private String expire(ConfirmationToken token) {
    confirmationTokenRepository.deleteById(token.getId());
    userService.deleteById(token.getUserIdx());
    return ConfirmMessage.EXPIRED.content;
  }

  @Override
  public void deleteByUserId(Long userIdx) {
    confirmationTokenRepository.deleteByUserIdx(userIdx);
  }
}

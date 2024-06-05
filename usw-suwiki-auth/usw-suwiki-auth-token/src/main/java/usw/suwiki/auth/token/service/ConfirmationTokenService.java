package usw.suwiki.auth.token.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.auth.token.ConfirmationToken;
import usw.suwiki.auth.token.ConfirmationTokenRepository;
import usw.suwiki.core.exception.AccountException;

import static usw.suwiki.auth.token.response.ConfirmResponse.ERROR;
import static usw.suwiki.auth.token.response.ConfirmResponse.EXPIRED;
import static usw.suwiki.auth.token.response.ConfirmResponse.SUCCESS;
import static usw.suwiki.core.exception.ExceptionCode.EMAIL_NOT_AUTHED;

@Service
@Transactional
@RequiredArgsConstructor
public class ConfirmationTokenService {
  private final ConfirmationTokenRepository confirmationTokenRepository;
  private final ConfirmUserService confirmUserService;

  public String requestConfirm(Long userId) {
    var confirmationToken = confirmationTokenRepository.save(new ConfirmationToken(userId));
    return confirmationToken.getToken();
  }

  public String confirm(String token) {
    return confirmationTokenRepository.findByToken(token)
      .map(it -> it.isExpired() ? expired(it) : confirm(it))
      .orElse(ERROR.getContent());
  }

  private String confirm(ConfirmationToken token) {
    token.confirm();
    confirmUserService.activate(token.getUserIdx());
    return SUCCESS.getContent();
  }

  private String expired(ConfirmationToken token) {
    confirmationTokenRepository.deleteById(token.getId());
    confirmUserService.delete(token.getUserIdx());
    return EXPIRED.getContent();
  }

  public void validateEmailAuthorized(Long userId) {
    confirmationTokenRepository.findByUserIdx(userId)
      .orElseThrow(() -> new AccountException(EMAIL_NOT_AUTHED))
      .validateVerified();
  }

  public void deleteByUserIdx(Long userIdx) {
    confirmationTokenRepository.deleteByUserIdx(userIdx);
  }
}

package usw.suwiki.auth.token.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.auth.token.ConfirmationToken;
import usw.suwiki.auth.token.ConfirmationTokenRepository;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConfirmationTokenCRUDService {
  private final ConfirmationTokenRepository confirmationTokenRepository;

  @Transactional
  public String save(Long userId) {
    var confirmationToken = confirmationTokenRepository.save(new ConfirmationToken(userId));
    return confirmationToken.getToken();
  }

  public Optional<ConfirmationToken> findOptionalTokenByUserId(Long userIdx) {
    return confirmationTokenRepository.findByUserIdx(userIdx);
  }

  public Optional<ConfirmationToken> findOptionalTokenByPayload(String payload) {
    return confirmationTokenRepository.findByToken(payload);
  }

  @Transactional
  public void deleteFromId(Long id) {
    confirmationTokenRepository.deleteById(id);
  }

  @Transactional
  public void deleteFromUserIdx(Long userIdx) {
    confirmationTokenRepository.deleteByUserIdx(userIdx);
  }
}

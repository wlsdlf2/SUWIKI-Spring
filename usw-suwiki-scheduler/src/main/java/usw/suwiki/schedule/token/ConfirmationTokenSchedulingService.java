package usw.suwiki.schedule.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.auth.token.ConfirmationToken;
import usw.suwiki.auth.token.ConfirmationTokenRepository;
import usw.suwiki.domain.user.service.UserCRUDService;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ConfirmationTokenSchedulingService {
  private final UserCRUDService userCRUDService;
  private final ConfirmationTokenRepository confirmationTokenRepository;

  @Scheduled(cron = "0 0 * * * * ")
  public void deleteAllUnconfirmedUsers() {
    log.info("{} - 이메일 인증을 수행하지 않은 유저 검증 시작", LocalDateTime.now());
    // TODO web hook
    var tokens = confirmationTokenRepository.findAllByExpiresAtBeforeAndConfirmedAtIsNull(LocalDateTime.now().minusMinutes(30));

    userCRUDService.deleteAllInBatch(tokens.stream().map(ConfirmationToken::getUserIdx).toList());
    confirmationTokenRepository.deleteAllInBatch(tokens);

    log.info("{} - 이메일 인증을 수행하지 않은 유저 검증 종료", LocalDateTime.now());
  }
}

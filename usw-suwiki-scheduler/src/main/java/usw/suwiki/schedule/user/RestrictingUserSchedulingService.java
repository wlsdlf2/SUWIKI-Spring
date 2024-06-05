package usw.suwiki.schedule.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.service.RestrictService;
import usw.suwiki.domain.user.service.UserService;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RestrictingUserSchedulingService {
  private final UserService userService;
  private final RestrictService restrictService;

  @Scheduled(cron = "0 0 * * * *")
  public void isUnrestrictedTarget() {
    log.info("{} - 정지 유저 출소 시작", LocalDateTime.now());
    for (Long restrictedId : restrictService.loadAllRestrictedUntilNow()) {
      User user = userService.loadUserById(restrictedId); // todo: 한번에 처리하도록 수정
      user.released();
      restrictService.release(restrictedId);
    }

    log.info("{} - 정지 유저 출소 종료", LocalDateTime.now());
  }
}

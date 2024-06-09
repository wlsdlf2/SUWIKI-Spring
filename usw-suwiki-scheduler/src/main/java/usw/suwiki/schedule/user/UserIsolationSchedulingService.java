package usw.suwiki.schedule.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.auth.service.RefreshTokenService;
import usw.suwiki.core.mail.EmailSender;
import usw.suwiki.domain.evaluatepost.service.EvaluatePostService;
import usw.suwiki.domain.exampost.service.ExamPostService;
import usw.suwiki.domain.report.service.ReportService;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.service.ConfirmationTokenService;
import usw.suwiki.domain.user.service.FavoriteMajorService;
import usw.suwiki.domain.user.service.RestrictService;
import usw.suwiki.domain.user.service.UserIsolationService;
import usw.suwiki.domain.user.service.UserService;
import usw.suwiki.domain.viewexam.service.ViewExamService;

import java.time.LocalDateTime;

import static usw.suwiki.core.mail.MailType.DELETE_WARNING;
import static usw.suwiki.core.mail.MailType.DORMANT_NOTIFICATION;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserIsolationSchedulingService { // todo: (06.05) 스케쥴러 쪽 전부 쿼리로 뽑아오기 (가능하다면)
  private final EmailSender emailSender;

  private final UserService userService;
  private final RestrictService restrictService;
  private final UserIsolationService userIsolationService;

  private final ViewExamService viewExamService;
  private final FavoriteMajorService favoriteMajorService;

  private final ReportService reportService;

  private final EvaluatePostService evaluatePostService;
  private final ExamPostService examPostService;

  private final RefreshTokenService refreshTokenService;
  private final ConfirmationTokenService confirmationTokenService;

  @Scheduled(cron = "2 0 0 * * *")
  public void sendEmailAboutSleeping() {
    log.info("{} - 휴면 계정 대상들에게 이메일 전송 시작", LocalDateTime.now());

    LocalDateTime startTime = LocalDateTime.now().minusMonths(11).minusDays(1);
    LocalDateTime endTime = LocalDateTime.now().minusMonths(11);

    for (User user : userService.loadUsersLastLoginBetween(startTime, endTime)) {
      emailSender.send(user.getEmail(), DORMANT_NOTIFICATION);
    }

    log.info("{} - 휴면 계정 대상들에게 이메일 전송 종료", LocalDateTime.now());
  }

  @Scheduled(cron = "4 0 0 * * *")
  public void convertSleepingTable() {
    log.info("{} - 휴면 계정 전환 시작", LocalDateTime.now());

    LocalDateTime startTime = LocalDateTime.now().minusMonths(35);
    LocalDateTime endTime = LocalDateTime.now().minusMonths(12);

    for (User user : userService.loadUsersLastLoginBetween(startTime, endTime)) {
      userIsolationService.isolate(user.getId());
    }

    log.info("{} - 휴면 계정 전환 종료", LocalDateTime.now());
  }

  @Scheduled(cron = "6 0 0 * * *")
  public void sendEmailAutoDeleteTargeted() {
    log.info("{} - 자동 삭제 이메일 전송 시작", LocalDateTime.now());

    LocalDateTime startTime = LocalDateTime.now().minusMonths(36);
    LocalDateTime endTime = LocalDateTime.now().minusMonths(35);

    for (User user : userService.loadUsersLastLoginBetween(startTime, endTime)) {
      emailSender.send(user.getEmail(), DELETE_WARNING);
    }

    log.info("{} - 자동 삭제 이메일 전송 종료", LocalDateTime.now());
  }

  @Scheduled(cron = "8 0 0 * * *")
  public void autoDeleteTargetIsThreeYears() {
    log.info("{} - 자동 삭제 시작", LocalDateTime.now());

    LocalDateTime startTime = LocalDateTime.now().minusMonths(100);
    LocalDateTime endTime = LocalDateTime.now().minusMonths(36);

    for (var userId : userService.loadUsersLastLoginBetween(startTime, endTime).stream().map(User::getId).toList()) {
      viewExamService.clean(userId);
      refreshTokenService.deleteByUserId(userId);
      reportService.clean(userId);
      evaluatePostService.clean(userId);
      examPostService.clean(userId);
      favoriteMajorService.clean(userId);
      restrictService.release(userId);
      confirmationTokenService.deleteByUserId(userId);
      userIsolationService.deleteByUserId(userId);
      userService.deleteById(userId);
    }

    log.info("{} - 자동 삭제 종료", LocalDateTime.now());
  }
}

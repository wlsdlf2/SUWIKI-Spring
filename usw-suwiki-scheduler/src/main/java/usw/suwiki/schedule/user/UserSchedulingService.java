package usw.suwiki.schedule.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.auth.token.ConfirmationTokenRepository;
import usw.suwiki.auth.token.RefreshTokenRepository;
import usw.suwiki.core.mail.EmailSender;
import usw.suwiki.domain.evaluatepost.service.EvaluatePostService;
import usw.suwiki.domain.exampost.service.ExamPostService;
import usw.suwiki.domain.report.service.ReportService;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.UserRepository;
import usw.suwiki.domain.user.service.FavoriteMajorService;
import usw.suwiki.domain.user.service.RestrictService;
import usw.suwiki.domain.user.service.UserIsolationService;
import usw.suwiki.domain.viewexam.service.ViewExamService;

import java.time.LocalDateTime;

import static usw.suwiki.core.mail.MailType.PRIVACY_POLICY_NOTIFICATION;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserSchedulingService {
  private final EmailSender emailSender;

  private final UserRepository userRepository;
  private final ViewExamService viewExamService;
  private final FavoriteMajorService favoriteMajorService;
  private final RestrictService restrictService;
  private final UserIsolationService userIsolationService;

  private final RefreshTokenRepository refreshTokenRepository;
  private final ConfirmationTokenRepository confirmationTokenRepository;

  private final ReportService reportService;
  private final ExamPostService examPostService;
  private final EvaluatePostService evaluatePostService;

  @Scheduled(cron = "0 1 0 1 3 *")
  public void sendPrivacyPolicyMail() {
    log.info("{} - 개인정보 처리 방침 안내 발송 시작", LocalDateTime.now());

    userRepository.findAll()
      .forEach(user -> emailSender.send(user.getEmail(), PRIVACY_POLICY_NOTIFICATION));

    log.info("{} - 개인정보 처리 방침 안내 발송 종료", LocalDateTime.now());
  }

  @Scheduled(cron = "0 0 * * * *")
  public void deleteRequestQuitUserAfter30Days() {
    log.info("{} - 회원탈퇴 유저 제거 시작", LocalDateTime.now());

    LocalDateTime targetTime = LocalDateTime.now().minusDays(30);
    var ids = userRepository.findByRequestedQuitDateBefore(targetTime).stream()
      .map(User::getId)
      .toList();

    var targets = ids.isEmpty() ? userIsolationService.loadAllIsolatedUntil(targetTime) : ids;

    targets.forEach(id -> {
      viewExamService.clean(id);
      refreshTokenRepository.deleteByUserIdx(id);
      reportService.clean(id);
      evaluatePostService.clean(id);
      examPostService.clean(id);
      favoriteMajorService.clean(id);
      restrictService.release(id);
      confirmationTokenRepository.deleteByUserIdx(id);
      userRepository.deleteById(id);
    });

    log.info("{} - 회원탈퇴 유저 제거 종료", LocalDateTime.now());
  }
}

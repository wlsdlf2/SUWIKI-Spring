package usw.suwiki.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.core.secure.Encoder;
import usw.suwiki.core.secure.TokenGenerator;
import usw.suwiki.domain.evaluatepost.service.EvaluatePostService;
import usw.suwiki.domain.exampost.service.ExamPostService;
import usw.suwiki.domain.report.EvaluatePostReport;
import usw.suwiki.domain.report.ExamPostReport;
import usw.suwiki.domain.report.service.ReportService;
import usw.suwiki.domain.user.dto.AdminResponse;

import static usw.suwiki.domain.user.dto.AdminRequest.EvaluatePostBlacklist;
import static usw.suwiki.domain.user.dto.AdminRequest.ExamPostBlacklist;
import static usw.suwiki.domain.user.dto.AdminRequest.RestrictEvaluatePost;
import static usw.suwiki.domain.user.dto.AdminRequest.RestrictExamPost;
import static usw.suwiki.domain.user.dto.AdminResponse.LoadAllReportedPost;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {
  private final AuthService authService;
  private final UserService userService;

  private final ReportService reportService;
  private final ExamPostService examPostService;
  private final EvaluatePostService evaluatePostService;

  private final Encoder encoder;
  private final TokenGenerator tokenGenerator;

  public AdminResponse.Login adminLogin(String loginId, String password) {
    var admin = authService.loadForLogin(loginId);
    admin.validateAdmin();
    admin.validateLoginable(encoder, password);
    var tokens = tokenGenerator.login(admin.getId(), admin.toClaim());
    return new AdminResponse.Login(tokens.getAccessToken(), userService.countAllUsers());
  }

  public LoadAllReportedPost loadAllReportedPosts() {
    return new LoadAllReportedPost(
      reportService.loadAllExamReports(),
      reportService.loadAllEvaluateReports()
    );
  }

  public EvaluatePostReport loadDetailReportedEvaluatePost(Long evaluateReportId) {
    return reportService.loadEvaluateReportById(evaluateReportId);
  }

  public ExamPostReport loadDetailReportedExamPost(Long examPostReportId) {
    return reportService.loadExamReportById(examPostReportId);
  }

  // todo: (06.05) 비슷한 로직들 해결하기
  public void restrict(RestrictEvaluatePost request) {
    var evaluatePostReport = reportService.loadEvaluateReportById(request.evaluateIdx());

    userService.rewardReport(evaluatePostReport.getReportingUserIdx());
    userService.restrict(evaluatePostReport.getReportedUserIdx(), request.restrictingDate(), request.restrictingReason(), request.judgement());

    resolveEvaluateReport(evaluatePostReport.getEvaluateIdx());
  }

  public void restrict(RestrictExamPost request) {
    var examPostReport = reportService.loadExamReportById(request.examIdx());

    userService.rewardReport(examPostReport.getReportingUserIdx());
    userService.restrict(examPostReport.getReportedUserIdx(), request.restrictingDate(), request.restrictingReason(), request.judgement());

    resolveExamReport(examPostReport.getExamIdx());
  }

  public void black(EvaluatePostBlacklist request) {
    var evaluatePostReport = reportService.loadEvaluateReportById(request.evaluateIdx());

    userService.rewardReport(evaluatePostReport.getReportingUserIdx());
    userService.black(evaluatePostReport.getReportedUserIdx(), request.bannedReason(), request.judgement());

    resolveEvaluateReport(request.evaluateIdx());
  }

  public void black(ExamPostBlacklist request) {
    var examPostReport = reportService.loadExamReportById(request.examIdx());

    userService.rewardReport(examPostReport.getReportingUserIdx());
    userService.black(examPostReport.getReportedUserIdx(), request.bannedReason(), request.judgement());

    resolveExamReport(request.examIdx());
  }

  private void resolveEvaluateReport(Long evaluateId) {
    evaluatePostService.reported(evaluateId);
    dismissEvaluateReport(evaluateId);
  }

  private void resolveExamReport(Long examPostId) {
    examPostService.reported(examPostId);
    dismissExamReport(examPostId);
  }

  public void dismissEvaluateReport(Long evaluatePostId) {
    reportService.deleteByEvaluateId(evaluatePostId);
  }

  public void dismissExamReport(Long examId) {
    reportService.deleteByExamId(examId);
  }
}

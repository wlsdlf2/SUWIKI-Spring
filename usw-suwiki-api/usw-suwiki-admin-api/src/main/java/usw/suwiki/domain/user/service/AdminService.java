package usw.suwiki.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.core.exception.ExceptionType;
import usw.suwiki.core.secure.Encoder;
import usw.suwiki.core.secure.TokenAgent;
import usw.suwiki.domain.evaluatepost.service.EvaluatePostService;
import usw.suwiki.domain.exampost.service.ExamPostService;
import usw.suwiki.domain.report.EvaluatePostReport;
import usw.suwiki.domain.report.ExamPostReport;
import usw.suwiki.domain.report.service.ReportService;
import usw.suwiki.domain.user.User;
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
  private final UserService userService;

  private final ReportService reportService;
  private final ExamPostService examPostService;
  private final EvaluatePostService evaluatePostService;

  private final Encoder encoder;
  private final TokenAgent tokenAgent;

  public AdminResponse.Login adminLogin(String loginId, String password) {
    User user = userService.loadByLoginId(loginId);

    if (!user.isPasswordEquals(encoder, password)) {
      throw new AccountException(ExceptionType.PASSWORD_ERROR);
    }

    if (!user.isAdmin()) {
      throw new AccountException(ExceptionType.USER_RESTRICTED);
    }

    return new AdminResponse.Login(tokenAgent.createAccessToken(user.getId(), user.toClaim()), userService.countAllUsers());
  }

  public LoadAllReportedPost loadAllReportedPosts() {
    return new LoadAllReportedPost(
      reportService.loadAllExamReports(),
      reportService.loadAllEvaluateReports()
    );
  }

  public EvaluatePostReport loadDetailReportedEvaluatePost(Long evaluatePostId) {
    return reportService.loadEvaluateReportById(evaluatePostId);
  }

  public ExamPostReport loadDetailReportedExamPost(Long examPostReportId) {
    return reportService.loadExamReportById(examPostReportId);
  }

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
    Long userId = evaluatePostService.loadEvaluatePostById(request.evaluateIdx()).getUserIdx();
    userService.black(userId, request.bannedReason(), request.judgement());
    resolveEvaluateReport(request.evaluateIdx());
  }

  public void black(ExamPostBlacklist request) {
    Long userId = examPostService.loadExamPostById(request.examIdx()).getUserIdx();
    userService.black(userId, request.bannedReason(), request.judgement());
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

package usw.suwiki.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.core.exception.ExceptionType;
import usw.suwiki.core.secure.Encoder;
import usw.suwiki.core.secure.TokenAgent;
import usw.suwiki.domain.evaluatepost.service.EvaluatePostService;
import usw.suwiki.domain.exampost.service.ExamPostCRUDService;
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
  private final Encoder encoder;

  private final UserCRUDService userCRUDService;
  private final UserBusinessService userBusinessService;
  private final BlacklistService blacklistService;
  private final RestrictService restrictService;
  private final UserIsolationCRUDService userIsolationCRUDService;

  private final ReportService reportService;
  private final ExamPostCRUDService examPostCRUDService;
  private final EvaluatePostService evaluatePostService;

  private final TokenAgent tokenAgent;

  public AdminResponse.Login adminLogin(String loginId, String password) {
    User user = userCRUDService.loadByLoginId(loginId);

    if (!user.isPasswordEquals(encoder, password)) {
      throw new AccountException(ExceptionType.PASSWORD_ERROR);
    }

    if (!user.isAdmin()) {
      throw new AccountException(ExceptionType.USER_RESTRICTED);
    }

    final long userCount = userCRUDService.countAllUsers();
    final long userIsolationCount = userIsolationCRUDService.countAllIsolatedUsers();

    return new AdminResponse.Login(tokenAgent.createAccessToken(user.getId(), user.toClaim()), userCount + userIsolationCount);
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

  public void restrictReport(RestrictEvaluatePost request) {
    var evaluatePostReport = reportService.loadEvaluateReportById(request.evaluateIdx());

    userBusinessService.rewardReport(evaluatePostReport.getReportingUserIdx());

    restrictService.restrict(evaluatePostReport.getReportedUserIdx(), request.restrictingDate(), request.restrictingReason(), request.judgement());

    resolvedEvaluateReport(evaluatePostReport.getEvaluateIdx());
  }

  public void restrictReport(RestrictExamPost request) {
    var examPostReport = reportService.loadExamReportById(request.examIdx());

    userBusinessService.rewardReport(examPostReport.getReportingUserIdx());

    restrictService.restrict(examPostReport.getReportedUserIdx(), request.restrictingDate(), request.restrictingReason(), request.judgement());

    resolveExamReport(examPostReport.getExamIdx());
  }

  public void blackEvaluatePost(EvaluatePostBlacklist request) {
    Long userId = evaluatePostService.loadEvaluatePostById(request.evaluateIdx()).getUserIdx();

    resolvedEvaluateReport(request.evaluateIdx());

    blacklistService.black(userId, request.bannedReason(), request.judgement());
  }

  public void blackListExamPost(ExamPostBlacklist request) {
    Long userIdx = examPostCRUDService.loadExamPostById(request.examIdx()).getUserIdx();

    resolveExamReport(request.examIdx());

    blacklistService.black(userIdx, request.bannedReason(), request.judgement());
  }

  private void resolvedEvaluateReport(Long evaluateId) {
    var evaluatePost = evaluatePostService.loadEvaluatePostById(evaluateId);
    evaluatePostService.delete(evaluatePost);
    dismissEvaluateReport(evaluateId);
  }

  private void resolveExamReport(Long examPostId) {
    var examPost = examPostCRUDService.loadExamPostById(examPostId);
    examPostCRUDService.delete(examPost);
    dismissExamReport(examPostId);
  }

  public void dismissEvaluateReport(Long evaluatePostId) {
    reportService.deleteByEvaluateId(evaluatePostId);
  }

  public void dismissExamReport(Long examId) {
    reportService.deleteByExamId(examId);
  }
}

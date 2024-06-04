package usw.suwiki.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.core.exception.ExceptionType;
import usw.suwiki.core.secure.PasswordEncoder;
import usw.suwiki.core.secure.TokenAgent;
import usw.suwiki.domain.evaluatepost.EvaluatePost;
import usw.suwiki.domain.evaluatepost.service.EvaluatePostService;
import usw.suwiki.domain.exampost.ExamPost;
import usw.suwiki.domain.exampost.service.ExamPostCRUDService;
import usw.suwiki.domain.report.EvaluatePostReport;
import usw.suwiki.domain.report.ExamPostReport;
import usw.suwiki.domain.report.service.ReportService;
import usw.suwiki.domain.user.User;

import java.util.HashMap;
import java.util.Map;

import static usw.suwiki.domain.user.dto.AdminRequest.EvaluatePostBlacklist;
import static usw.suwiki.domain.user.dto.AdminRequest.EvaluatePostNoProblem;
import static usw.suwiki.domain.user.dto.AdminRequest.EvaluatePostRestricted;
import static usw.suwiki.domain.user.dto.AdminRequest.ExamPostBlacklist;
import static usw.suwiki.domain.user.dto.AdminRequest.ExamPostNoProblem;
import static usw.suwiki.domain.user.dto.AdminRequest.ExamPostRestricted;
import static usw.suwiki.domain.user.dto.AdminResponse.LoadAllReportedPost;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {
  private static final long BANNED_PERIOD = 365L;

  private final PasswordEncoder passwordEncoder;
  private final UserCRUDService userCRUDService;
  private final RestrictingUserService restrictingUserService;
  private final UserIsolationCRUDService userIsolationCRUDService;
  private final BlacklistDomainCRUDService blacklistDomainCRUDService;

  private final ReportService reportService;
  private final ExamPostCRUDService examPostCRUDService;
  private final EvaluatePostService evaluatePostService;

  private final TokenAgent tokenAgent;

  public Map<String, String> adminLogin(String loginId, String password) {
    User user = userCRUDService.loadByLoginId(loginId);

    if (!user.isPasswordEquals(passwordEncoder, password)) {
      throw new AccountException(ExceptionType.PASSWORD_ERROR);
    }

    if (!user.isAdmin()) {
      throw new AccountException(ExceptionType.USER_RESTRICTED);
    }

    final long userCount = userCRUDService.countAllUsers();
    final long userIsolationCount = userIsolationCRUDService.countAllIsolatedUsers();

    return new HashMap<>() {{
      put("AccessToken", tokenAgent.createAccessToken(user.getId(), user.toClaim()));
      put("UserCount", String.valueOf(userCount + userIsolationCount));
    }};
  }

  public LoadAllReportedPost loadAllReportedPosts() {
    return new LoadAllReportedPost(
      reportService.loadAllExamReports(),
      reportService.loadAllEvaluateReports()
    );
  }

  public EvaluatePostReport loadDetailReportedEvaluatePost(Long evaluatePostId) {
    return reportService.loadEvaluateReportByEvaluateId(evaluatePostId);
  }

  public ExamPostReport loadDetailReportedExamPost(Long examPostReportId) {
    return reportService.loadExamReportByExamId(examPostReportId);
  }

  public void deleteNoProblemEvaluatePost(EvaluatePostNoProblem evaluatePostNoProblem) {
    reportService.deleteByEvaluateIdx(evaluatePostNoProblem.evaluateIdx());
  }

  public void deleteNoProblemExamPost(ExamPostNoProblem examPostRestrictForm) {
    reportService.deleteByExamIdx(examPostRestrictForm.examIdx());
  }

  public void restrictEvaluatePost(EvaluatePostRestricted request) {
    EvaluatePostReport evaluatePostReport = reportService.loadEvaluateReportByEvaluateId(request.evaluateIdx());

    plusReportingUserPoint(evaluatePostReport.getReportingUserIdx());
    plusRestrictCount(evaluatePostReport.getReportedUserIdx());

    restrictingUserService.restrictFromEvaluatePost(request, evaluatePostReport.getReportedUserIdx());

    deleteReportedEvaluatePostByEvaluateId(evaluatePostReport.getEvaluateIdx());
  }

  public void restrictExamPost(ExamPostRestricted request) {
    ExamPostReport examPostReport = reportService.loadExamReportByExamId(request.examIdx());

    plusReportingUserPoint(examPostReport.getReportingUserIdx());
    plusRestrictCount(examPostReport.getReportedUserIdx());

    restrictingUserService.restrictFromExamPost(request, examPostReport.getReportedUserIdx());

    deleteReportedExamPostByEvaluateId(examPostReport.getExamIdx());
  }

  public void blackEvaluatePost(EvaluatePostBlacklist evaluatePostBlacklist) {
    Long userIdx = evaluatePostService.loadEvaluatePostById(evaluatePostBlacklist.evaluateIdx()).getUserIdx();

    deleteReportedEvaluatePostByEvaluateId(evaluatePostBlacklist.evaluateIdx());

    blacklistDomainCRUDService.saveBlackListDomain(
      userIdx,
      BANNED_PERIOD,
      evaluatePostBlacklist.bannedReason(),
      evaluatePostBlacklist.judgement()
    );
    plusRestrictCount(userIdx);
  }

  public void blackListExamPost(ExamPostBlacklist examPostBlacklist) {
    Long userIdx = examPostCRUDService.loadExamPostFromExamPostIdx(examPostBlacklist.examIdx()).getUserIdx();

    deleteReportedExamPostByEvaluateId(examPostBlacklist.examIdx());
    blacklistDomainCRUDService.saveBlackListDomain(
      userIdx,
      BANNED_PERIOD,
      examPostBlacklist.bannedReason(),
      examPostBlacklist.judgement()
    );
    plusRestrictCount(userIdx);
  }

  private void deleteReportedEvaluatePostByEvaluateId(Long evaluateId) {
    EvaluatePost evaluatePost = evaluatePostService.loadEvaluatePostById(evaluateId);
    reportService.deleteByEvaluateIdx(evaluateId);
    evaluatePostService.delete(evaluatePost);
  }

  private void deleteReportedExamPostByEvaluateId(Long examPostId) {
    ExamPost examPost = examPostCRUDService.loadExamPostFromExamPostIdx(examPostId);
    reportService.deleteByEvaluateIdx(examPostId);
    examPostCRUDService.delete(examPost);
  }

  private void plusRestrictCount(Long userId) {
    User user = userCRUDService.loadUserById(userId);
    user.reported();
  }

  private void plusReportingUserPoint(Long reportingUserId) {
    User user = userCRUDService.loadUserById(reportingUserId);
    user.report();
  }
}

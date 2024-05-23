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

import java.util.List;
import java.util.Map;

import static usw.suwiki.common.response.ApiResponseFactory.adminLoginResponseForm;
import static usw.suwiki.common.response.ApiResponseFactory.successCapitalFlag;
import static usw.suwiki.domain.user.dto.UserAdminRequestDto.EvaluatePostBlacklistForm;
import static usw.suwiki.domain.user.dto.UserAdminRequestDto.EvaluatePostNoProblemForm;
import static usw.suwiki.domain.user.dto.UserAdminRequestDto.EvaluatePostRestrictForm;
import static usw.suwiki.domain.user.dto.UserAdminRequestDto.ExamPostBlacklistForm;
import static usw.suwiki.domain.user.dto.UserAdminRequestDto.ExamPostNoProblemForm;
import static usw.suwiki.domain.user.dto.UserAdminRequestDto.ExamPostRestrictForm;
import static usw.suwiki.domain.user.dto.UserAdminResponseDto.LoadAllReportedPostForm;
import static usw.suwiki.domain.user.dto.UserRequestDto.LoginForm;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminBusinessService {
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

  public Map<String, String> adminLogin(LoginForm loginForm) {
    User user = userCRUDService.loadUserFromLoginId(loginForm.loginId());
    if (user.isPasswordEquals(passwordEncoder, loginForm.password())) {
      if (user.isAdmin()) {
        final long userCount = userCRUDService.countAllUsers();
        final long userIsolationCount = userIsolationCRUDService.countAllIsolatedUsers();
        final long totalUserCount = userCount + userIsolationCount;
        var claim = user.toClaim();

        return adminLoginResponseForm(tokenAgent.createAccessToken(user.getId(), claim), String.valueOf(totalUserCount));
      }
      throw new AccountException(ExceptionType.USER_RESTRICTED);
    }
    throw new AccountException(ExceptionType.PASSWORD_ERROR);
  }

  public LoadAllReportedPostForm executeLoadAllReportedPosts() {
    List<EvaluatePostReport> evaluatePostReports = reportService.loadAllEvaluateReports();
    List<ExamPostReport> examPostReports = reportService.loadAllExamReports();

    return new LoadAllReportedPostForm(examPostReports, evaluatePostReports);
  }

  public EvaluatePostReport executeLoadDetailReportedEvaluatePost(Long evaluatePostReportId) {
    return reportService.loadEvaluateReportByEvaluateId(evaluatePostReportId);
  }

  public ExamPostReport executeLoadDetailReportedExamPost(Long examPostReportId) {
    return reportService.loadExamReportByExamId(examPostReportId);
  }

  public Map<String, Boolean> executeNoProblemEvaluatePost(EvaluatePostNoProblemForm evaluatePostNoProblemForm) {
    reportService.deleteByEvaluateIdx(evaluatePostNoProblemForm.evaluateIdx());
    return successCapitalFlag();
  }

  public Map<String, Boolean> executeNoProblemExamPost(ExamPostNoProblemForm examPostRestrictForm) {
    reportService.deleteByExamIdx(examPostRestrictForm.examIdx());
    return successCapitalFlag();
  }

  public Map<String, Boolean> executeRestrictEvaluatePost(EvaluatePostRestrictForm request) {
    EvaluatePostReport evaluatePostReport = reportService.loadEvaluateReportByEvaluateId(request.evaluateIdx());

    plusReportingUserPoint(evaluatePostReport.getReportingUserIdx());
    plusRestrictCount(evaluatePostReport.getReportedUserIdx());

    restrictingUserService.executeRestrictUserFromEvaluatePost(request, evaluatePostReport.getReportedUserIdx());

    deleteReportedEvaluatePostFromEvaluateIdx(evaluatePostReport.getEvaluateIdx());
    return successCapitalFlag();
  }

  public Map<String, Boolean> executeRestrictExamPost(ExamPostRestrictForm request) {
    ExamPostReport examPostReport = reportService.loadExamReportByExamId(request.examIdx());

    plusReportingUserPoint(examPostReport.getReportingUserIdx());
    plusRestrictCount(examPostReport.getReportedUserIdx());

    restrictingUserService.executeRestrictUserFromExamPost(request, examPostReport.getReportedUserIdx());

    deleteReportedExamPostFromEvaluateIdx(examPostReport.getExamIdx());
    return successCapitalFlag();
  }

  public Map<String, Boolean> executeBlackListEvaluatePost(EvaluatePostBlacklistForm evaluatePostBlacklistForm) {
    Long userIdx = evaluatePostService.loadEvaluatePostById(evaluatePostBlacklistForm.evaluateIdx()).getUserIdx();

    deleteReportedEvaluatePostFromEvaluateIdx(evaluatePostBlacklistForm.evaluateIdx());

    blacklistDomainCRUDService.saveBlackListDomain(
      userIdx,
      BANNED_PERIOD,
      evaluatePostBlacklistForm.bannedReason(),
      evaluatePostBlacklistForm.judgement()
    );
    plusRestrictCount(userIdx);

    return successCapitalFlag();
  }

  public Map<String, Boolean> executeBlackListExamPost(ExamPostBlacklistForm examPostBlacklistForm) {
    Long userIdx = examPostCRUDService.loadExamPostFromExamPostIdx(examPostBlacklistForm.examIdx()).getUserIdx();

    deleteReportedExamPostFromEvaluateIdx(examPostBlacklistForm.examIdx());
    blacklistDomainCRUDService.saveBlackListDomain(
      userIdx,
      365L,
      examPostBlacklistForm.bannedReason(),
      examPostBlacklistForm.judgement()
    );
    plusRestrictCount(userIdx);

    return successCapitalFlag();
  }

  private void deleteReportedEvaluatePostFromEvaluateIdx(Long evaluateId) {
    EvaluatePost evaluatePost = evaluatePostService.loadEvaluatePostById(evaluateId);
    reportService.deleteByEvaluateIdx(evaluateId);
    evaluatePostService.delete(evaluatePost);
  }

  private void deleteReportedExamPostFromEvaluateIdx(Long examPostId) {
    ExamPost examPost = examPostCRUDService.loadExamPostFromExamPostIdx(examPostId);
    reportService.deleteByEvaluateIdx(examPostId);
    examPostCRUDService.delete(examPost);
  }

  private void plusRestrictCount(Long userId) {
    User user = userCRUDService.loadUserFromUserIdx(userId);
    user.reported();
  }

  private void plusReportingUserPoint(Long reportingUserId) {
    User user = userCRUDService.loadUserFromUserIdx(reportingUserId);
    user.report();
  }
}

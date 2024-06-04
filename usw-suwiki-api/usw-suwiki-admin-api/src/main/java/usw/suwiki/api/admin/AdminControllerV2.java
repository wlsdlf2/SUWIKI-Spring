package usw.suwiki.api.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import usw.suwiki.auth.core.annotation.Authorize;
import usw.suwiki.domain.report.EvaluatePostReport;
import usw.suwiki.domain.report.ExamPostReport;
import usw.suwiki.domain.user.Role;
import usw.suwiki.domain.user.dto.AdminRequest;
import usw.suwiki.domain.user.dto.AdminResponse;
import usw.suwiki.domain.user.dto.UserRequest;
import usw.suwiki.domain.user.service.AdminService;
import usw.suwiki.statistics.annotation.Statistics;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;
import static usw.suwiki.statistics.log.MonitorTarget.ADMIN;

@RestController
@RequestMapping("/v2/admin")
@RequiredArgsConstructor
public class AdminControllerV2 {
  private final AdminService adminService;

  @Statistics(ADMIN)
  @PostMapping("/login")
  @ResponseStatus(OK)
  public AdminResponse.Login adminLogin(@Valid @RequestBody UserRequest.Login request) {
    return adminService.adminLogin(request.loginId(), request.password());
  }

  @Authorize(Role.ADMIN)
  @Statistics(ADMIN)
  @PostMapping("/evaluate-posts/restrict")
  @ResponseStatus(OK)
  public Map<String, Boolean> restrictEvaluatePost(@Valid @RequestBody AdminRequest.RestrictEvaluatePost request) {
    adminService.restrictReport(request);
    return success();
  }

  @Authorize(Role.ADMIN)
  @ResponseStatus(OK)
  @Statistics(ADMIN)
  @PostMapping("/exam-post/restrict")
  public Map<String, Boolean> restrictExamPost(@Valid @RequestBody AdminRequest.RestrictExamPost request) {
    adminService.restrictReport(request);
    return success();
  }

  @Authorize(Role.ADMIN)
  @ResponseStatus(OK)
  @Statistics(ADMIN)
  @PostMapping("/evaluate-post/blacklist")
  public Map<String, Boolean> banEvaluatePost(@Valid @RequestBody AdminRequest.EvaluatePostBlacklist request) {
    adminService.blackEvaluatePost(request);
    return success();
  }

  @Authorize(Role.ADMIN)
  @ResponseStatus(OK)
  @Statistics(ADMIN)
  @PostMapping("/exam-post/blacklist")
  public Map<String, Boolean> banExamPost(@Valid @RequestBody AdminRequest.ExamPostBlacklist request) {
    adminService.blackListExamPost(request);
    return success();
  }

  @Authorize(Role.ADMIN)
  @ResponseStatus(OK)
  @Statistics(ADMIN)
  @DeleteMapping("/evaluate-post")
  public Map<String, Boolean> noProblemEvaluatePost(@Valid @RequestBody AdminRequest.EvaluatePostNoProblem request) {
    adminService.dismissEvaluateReport(request.evaluateIdx());
    return success();
  }

  @Authorize(Role.ADMIN)
  @ResponseStatus(OK)
  @Statistics(ADMIN)
  @DeleteMapping("/exam-post")
  public Map<String, Boolean> noProblemExamPost(@Valid @RequestBody AdminRequest.ExamPostNoProblem request) {
    adminService.dismissExamReport(request.examIdx());
    return success();
  }

  @Authorize(Role.ADMIN)
  @Statistics(ADMIN)
  @GetMapping("/reported-posts")
  @ResponseStatus(OK)
  public AdminResponse.LoadAllReportedPost loadReportedPost() {
    return adminService.loadAllReportedPosts();
  }

  @Authorize(Role.ADMIN)
  @Statistics(ADMIN)
  @GetMapping("/reported-evaluate/")
  @ResponseStatus(OK)
  public EvaluatePostReport loadDetailReportedEvaluatePost(@RequestParam Long target) {
    return adminService.loadDetailReportedEvaluatePost(target);
  }

  @Authorize(Role.ADMIN)
  @Statistics(ADMIN)
  @GetMapping("/reported-exam/")
  @ResponseStatus(OK)
  public ExamPostReport loadDetailReportedExamPost(@RequestParam Long target) {
    return adminService.loadDetailReportedExamPost(target);
  }

  private Map<String, Boolean> success() { // legacy
    return new HashMap<>() {{
      put("Success", true);
    }};
  }
}

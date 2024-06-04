package usw.suwiki.domain.user.dto;

import lombok.Data;
import usw.suwiki.domain.report.EvaluatePostReport;
import usw.suwiki.domain.report.ExamPostReport;

import java.util.List;

public final class AdminResponse {
  
  @Data
  public static class Login {
    private final String accessToken;
    private final Long userCount;
  }

  public record LoadAllReportedPost(
    List<ExamPostReport> examPostReports,
    List<EvaluatePostReport> evaluatePostReports
  ) {
  }
}

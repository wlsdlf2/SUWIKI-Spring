package usw.suwiki.common.test.fixture;

import usw.suwiki.domain.report.EvaluatePostReport;
import usw.suwiki.domain.report.ExamPostReport;
import usw.suwiki.domain.report.model.Report;

public class ReportFixture {

  private ReportFixture() {
  }

  public static EvaluatePostReport evaluate(Long reporter, Long reported, Long target) {
    return EvaluatePostReport.from(report(reporter, reported, target));
  }

  public static ExamPostReport exam(Long reporter, Long reported, Long target) {
    return ExamPostReport.from(report(reporter, reported, target));
  }

  private static Report report(Long reporter, Long reported, Long target) {
    return new Report(target, reported, reporter, "내용", "강의", "교수");
  }
}

package usw.suwiki.domain.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvaluateReportRepository extends JpaRepository<EvaluatePostReport, Long> {

  void deleteByEvaluateIdx(Long evaluateIdx);

  @Modifying
  @Query(value = "DELETE FROM evaluate_post_report where reporting_user_idx =:userIdx", nativeQuery = true)
  void deleteAllByReportingUserIdx(@Param("userIdx") Long userIdx);

  @Modifying
  @Query(value = "DELETE FROM evaluate_post_report where reported_user_idx =:userIdx", nativeQuery = true)
  void deleteAllByReportedUserIdx(@Param("userIdx") Long userIdx);
}

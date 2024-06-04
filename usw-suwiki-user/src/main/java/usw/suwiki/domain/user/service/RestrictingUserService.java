package usw.suwiki.domain.user.service;

import java.util.List;

import static usw.suwiki.domain.user.dto.AdminRequest.EvaluatePostRestricted;
import static usw.suwiki.domain.user.dto.AdminRequest.ExamPostRestricted;

public interface RestrictingUserService {
  List<Long> loadAllRestrictedUntilNow();

  void release(Long userId);

  void restrictFromEvaluatePost(EvaluatePostRestricted evaluatePostRestricted, Long reportedUserId);

  void restrictFromExamPost(ExamPostRestricted examPostRestricted, Long reportedUserId);
}

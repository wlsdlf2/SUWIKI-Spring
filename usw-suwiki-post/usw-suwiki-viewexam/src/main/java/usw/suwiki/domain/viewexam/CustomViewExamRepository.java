package usw.suwiki.domain.viewexam;

import java.util.List;

public interface CustomViewExamRepository {
  List<ViewExam> findByUserId(Long userIdx);

  boolean isExists(Long userId, Long lectureId);
}

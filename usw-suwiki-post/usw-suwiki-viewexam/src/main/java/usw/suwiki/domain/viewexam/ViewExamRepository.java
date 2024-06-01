package usw.suwiki.domain.viewexam;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ViewExamRepository extends JpaRepository<ViewExam, Long> {
  boolean existsByUserIdxAndLectureId(Long userIdx, Long lectureId);

  List<ViewExam> findAllByUserIdx(Long userIdx);
}

package usw.suwiki.domain.viewexam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.domain.viewexam.ViewExam;
import usw.suwiki.domain.viewexam.ViewExamRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class ViewExamService {
  private final ViewExamRepository viewExamRepository;

  public boolean isExist(Long userId, Long lectureId) {
    return viewExamRepository.existsByUserIdxAndLectureId(userId, lectureId);
  }

  public void purchase(Long userId, Long lectureId) {
    viewExamRepository.save(new ViewExam(userId, lectureId));
  }

  public void clean(Long userId) {
    var viewExams = viewExamRepository.findAllByUserIdx(userId);
    viewExamRepository.deleteAllInBatch(viewExams);
  }
}

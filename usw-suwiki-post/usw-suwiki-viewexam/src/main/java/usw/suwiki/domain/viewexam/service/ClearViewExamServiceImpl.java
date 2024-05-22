package usw.suwiki.domain.viewexam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.domain.user.service.ClearViewExamService;
import usw.suwiki.domain.viewexam.ViewExamRepository;

@Service
@Transactional
@RequiredArgsConstructor
class ClearViewExamServiceImpl implements ClearViewExamService {
  private final ViewExamRepository viewExamRepository;

  @Override
  public void clear(Long userId) {
    var viewExams = viewExamRepository.findByUserId(userId);
    viewExamRepository.deleteAllInBatch(viewExams);
  }
}

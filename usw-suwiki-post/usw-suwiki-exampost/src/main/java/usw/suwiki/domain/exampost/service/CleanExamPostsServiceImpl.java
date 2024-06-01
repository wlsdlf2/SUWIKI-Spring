package usw.suwiki.domain.exampost.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.domain.exampost.ExamPost;
import usw.suwiki.domain.exampost.ExamPostRepository;
import usw.suwiki.domain.user.service.CleanExamPostsService;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
class CleanExamPostsServiceImpl implements CleanExamPostsService {
  private final ExamPostRepository examPostRepository;

  @Override
  public void clean(Long userId) {
    List<ExamPost> examPosts = examPostRepository.findAllByUserIdx(userId);
    examPostRepository.deleteAllInBatch(examPosts);
  }
}

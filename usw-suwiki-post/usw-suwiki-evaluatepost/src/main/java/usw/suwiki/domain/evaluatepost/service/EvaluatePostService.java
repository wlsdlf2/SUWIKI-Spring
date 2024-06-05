package usw.suwiki.domain.evaluatepost.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.common.pagination.PageOption;
import usw.suwiki.core.exception.EvaluatePostException;
import usw.suwiki.domain.evaluatepost.EvaluatePost;
import usw.suwiki.domain.evaluatepost.EvaluatePostQueryRepository;
import usw.suwiki.domain.evaluatepost.EvaluatePostRepository;
import usw.suwiki.domain.evaluatepost.dto.EvaluatePostRequest;
import usw.suwiki.domain.evaluatepost.dto.EvaluatePostResponse;
import usw.suwiki.domain.lecture.service.LectureService;
import usw.suwiki.domain.report.model.Report;
import usw.suwiki.domain.report.service.ReportService;
import usw.suwiki.domain.user.service.UserService;

import java.util.List;

import static usw.suwiki.core.exception.ExceptionCode.ALREADY_WROTE_EXAM_POST;
import static usw.suwiki.core.exception.ExceptionCode.EVALUATE_POST_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
public class EvaluatePostService {
  private final EvaluatePostRepository evaluatePostRepository;
  private final EvaluatePostQueryRepository evaluatePostQueryRepository;

  private final UserService userService;
  private final LectureService lectureService;

  private final ReportService reportService;

  @Transactional(readOnly = true)
  public EvaluatePostResponse.Details loadAllEvaluatePostsByLectureId(PageOption option, Long userId, Long lectureId) {
    return new EvaluatePostResponse.Details(
      evaluatePostQueryRepository.findAllByLectureIdAndPageOption(lectureId, option.getOffset()),
      isAlreadyWritten(userId, lectureId)
    );
  }

  @Transactional(readOnly = true)
  public List<EvaluatePostResponse.MyPost> loadAllEvaluatePostsByUserId(PageOption option, Long userId) {
    return evaluatePostQueryRepository.findAllByUserIdAndPageOption(userId, option.getOffset());
  }

  public EvaluatePost loadEvaluatePostById(Long evaluateId) {
    return evaluatePostRepository.findById(evaluateId)
      .orElseThrow(() -> new EvaluatePostException(EVALUATE_POST_NOT_FOUND));
  }

  public void report(Long reportingUserId, Long evaluateId) {
    var evaluatePost = loadEvaluatePostById(evaluateId);
    var report = Report.evaluate(evaluateId, evaluatePost.getUserIdx(), reportingUserId, evaluatePost.getContent(), evaluatePost.getLectureName(), evaluatePost.getProfessor());
    reportService.reportEvaluatePost(report);
  }

  public void reported(Long evaluateId) {
    var evaluatePost = loadEvaluatePostById(evaluateId);
    evaluatePostRepository.delete(evaluatePost);
  }

  public void write(Long userId, Long lectureId, EvaluatePostRequest.Create request) {
    if (isAlreadyWritten(userId, lectureId)) {
      throw new EvaluatePostException(ALREADY_WROTE_EXAM_POST);
    }

    var evaluatePost = EvaluatePostMapper.toEntity(userId, lectureId, request);
    var evaluation = EvaluatePostMapper.toEvaluatedData(evaluatePost.getLectureRating());

    lectureService.evaluate(lectureId, evaluation);
    evaluatePostRepository.save(evaluatePost);
    userService.evaluate(userId);
  }

  private boolean isAlreadyWritten(Long userId, Long lectureId) {
    return evaluatePostRepository.existsByUserIdxAndLectureInfo_LectureId(userId, lectureId);
  }

  public void update(Long userId, Long evaluateId, EvaluatePostRequest.Update request) {
    var evaluatePost = loadEvaluatePostById(evaluateId);
    evaluatePost.validateAuthor(userId);

    var currentEvaluation = EvaluatePostMapper.toEvaluatedData(evaluatePost.getLectureRating());

    evaluatePost.update(
      request.getContent(),
      evaluatePost.getLectureName(),
      request.getSelectedSemester(),
      evaluatePost.getProfessor(),
      EvaluatePostMapper.toRating(request)
    );

    var updatedEvaluation = EvaluatePostMapper.toEvaluatedData(evaluatePost.getLectureRating());
    lectureService.updateEvaluation(evaluatePost.getLectureId(), currentEvaluation, updatedEvaluation);
  }

  public void erase(Long userId, Long evaluateId) {
    var evaluatePost = loadEvaluatePostById(evaluateId);
    evaluatePost.validateAuthor(userId);

    evaluatePostRepository.delete(evaluatePost);
    userService.eraseEvaluation(userId);
  }

  public void clean(Long userId) {
    List<EvaluatePost> evaluatePosts = evaluatePostRepository.findAllByUserIdx(userId);
    evaluatePostRepository.deleteAllInBatch(evaluatePosts);
  }
}

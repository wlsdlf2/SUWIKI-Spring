package usw.suwiki.domain.exampost.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.common.pagination.PageOption;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.core.exception.ExamPostException;
import usw.suwiki.core.exception.ExceptionType;
import usw.suwiki.domain.exampost.ExamPost;
import usw.suwiki.domain.exampost.ExamPostQueryRepository;
import usw.suwiki.domain.exampost.ExamPostRepository;
import usw.suwiki.domain.exampost.dto.ExamPostRequest;
import usw.suwiki.domain.lecture.service.LectureService;
import usw.suwiki.domain.report.model.Report;
import usw.suwiki.domain.report.service.ReportService;
import usw.suwiki.domain.user.service.UserService;
import usw.suwiki.domain.viewexam.ViewExamQueryRepository;
import usw.suwiki.domain.viewexam.dto.ViewExamResponse;
import usw.suwiki.domain.viewexam.service.ViewExamService;

import java.util.List;

import static usw.suwiki.core.exception.ExceptionType.ALREADY_WROTE_EXAM_POST;
import static usw.suwiki.core.exception.ExceptionType.EXAM_POST_ALREADY_PURCHASE;
import static usw.suwiki.domain.exampost.dto.ExamPostResponse.Detail;
import static usw.suwiki.domain.exampost.dto.ExamPostResponse.Details;
import static usw.suwiki.domain.exampost.dto.ExamPostResponse.MyPost;

@Service
@Transactional
@RequiredArgsConstructor
public class ExamPostService {
  private static final int PAGE_LIMIT = 10; // todo: hide when query repository testable

  private final ExamPostRepository examPostRepository;
  private final ExamPostQueryRepository examPostQueryRepository;

  private final LectureService lectureService;
  private final UserService userService;
  private final ReportService reportService;

  private final ViewExamService viewExamService;
  private final ViewExamQueryRepository viewExamQueryRepository;

  @Transactional(readOnly = true)
  public List<ViewExamResponse.PurchaseHistory> loadPurchasedHistories(Long userId) {
    return viewExamQueryRepository.loadPurchasedHistoriesByUserId(userId);
  }

  @Transactional(readOnly = true)
  public Details loadAllExamPosts(Long userId, Long lectureId, PageOption option) {
    boolean isWritten = isAlreadyWritten(userId, lectureId);

    List<Detail> data = examPostRepository.findAllByLectureIdAndPageOption(lectureId, option.getOffset(), PAGE_LIMIT).stream()
      .map(ExamPostMapper::toDetail)
      .toList(); // todo: to query repository

    Details response = data.isEmpty() ? Details.noData(isWritten) : Details.withData(data, isWritten);

    if (!isAlreadyPurchased(userId, lectureId)) {
      response.noAccess();
    }

    return response;
  }

  @Transactional(readOnly = true)
  public List<MyPost> loadAllMyExamPosts(PageOption option, Long userId) {
    return examPostQueryRepository.findAllByUserIdAndPageOption(userId, option.getOffset());
  }

  public ExamPost loadExamPostById(Long examId) {
    return examPostRepository.findById(examId)
      .orElseThrow(() -> new ExamPostException(ExceptionType.EXAM_POST_NOT_FOUND));
  }

  public void report(Long reportingUserId, Long examId) {
    ExamPost examPost = loadExamPostById(examId);
    Long reportedUserId = examPost.getUserIdx();

    Report report = Report.exam(examId, reportedUserId, reportingUserId, examPost.getContent(), examPost.getLectureName(), examPost.getProfessor());
    reportService.reportExamPost(report);
  }

  public void reported(Long examPostId) {
    var examPost = loadExamPostById(examPostId);
    examPostRepository.delete(examPost);
  }

  public void write(Long userId, Long lectureId, ExamPostRequest.Create request) {
    if (isAlreadyWritten(userId, lectureId)) {
      throw new AccountException(ALREADY_WROTE_EXAM_POST);
    }

    lectureService.findLectureById(lectureId);
    examPostRepository.save(ExamPostMapper.toEntity(userId, lectureId, request));
    userService.writeExamPost(userId);
  }

  private boolean isAlreadyWritten(Long userId, Long lectureId) {
    return examPostRepository.existsByUserIdxAndLectureInfo_LectureId(userId, lectureId);
  }

  public void purchaseExamPost(Long userId, Long lectureId) {
    if (isAlreadyPurchased(userId, lectureId)) {
      throw new ExamPostException(EXAM_POST_ALREADY_PURCHASE);
    }

    lectureService.findLectureById(lectureId); // todo: 존재하는지 확인하는 용도. 나중에 유의미한 메서드로 수정할 것
    viewExamService.purchase(userId, lectureId);
    userService.purchaseExamPost(userId);
  }

  private boolean isAlreadyPurchased(Long userId, Long lectureId) {
    return viewExamService.isExist(userId, lectureId);
  }

  public void update(Long userId, Long examId, ExamPostRequest.Update request) {
    var examPost = loadExamPostById(examId);
    examPost.validateAuthor(userId);
    examPost.update(request.getContent(), request.getSelectedSemester(), ExamPostMapper.toExamDetail(request));
  }

  public void deleteExamPost(Long userId, Long examId) {
    var examPost = loadExamPostById(examId);
    examPost.validateAuthor(userId);

    examPostRepository.delete(examPost);
    userService.eraseExamPost(userId);
  }

  public void clean(Long userId) {
    List<ExamPost> examPosts = examPostRepository.findAllByUserIdx(userId);
    examPostRepository.deleteAllInBatch(examPosts);
  }
}

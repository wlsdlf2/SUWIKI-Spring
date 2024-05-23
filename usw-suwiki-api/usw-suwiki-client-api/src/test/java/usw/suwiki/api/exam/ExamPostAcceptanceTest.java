package usw.suwiki.api.exam;

import io.github.hejow.restdocs.document.RestDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import usw.suwiki.common.test.annotation.AcceptanceTest;
import usw.suwiki.common.test.support.AcceptanceTestSupport;
import usw.suwiki.common.test.support.Uri;
import usw.suwiki.domain.exampost.ExamPostRepository;
import usw.suwiki.domain.exampost.dto.ExamPostRequest;
import usw.suwiki.domain.lecture.Lecture;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.UserRepository;
import usw.suwiki.domain.viewexam.ViewExamRepository;
import usw.suwiki.test.fixture.Fixtures;

import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static usw.suwiki.common.test.Tag.EXAM_POST;
import static usw.suwiki.common.test.extension.AssertExtension.expectExceptionJsonPath;
import static usw.suwiki.common.test.support.Pair.parameter;
import static usw.suwiki.core.exception.ExceptionType.ALREADY_WROTE_EXAM_POST;
import static usw.suwiki.core.exception.ExceptionType.EXAM_POST_ALREADY_PURCHASE;
import static usw.suwiki.core.exception.ExceptionType.EXAM_POST_NOT_FOUND;
import static usw.suwiki.core.exception.ExceptionType.LECTURE_NOT_FOUND;
import static usw.suwiki.core.exception.ExceptionType.NOT_AN_AUTHOR;
import static usw.suwiki.core.exception.ExceptionType.USER_POINT_LACK;

@AcceptanceTest
public class ExamPostAcceptanceTest extends AcceptanceTestSupport {
  @Autowired
  private ExamPostRepository examPostRepository;
  @Autowired
  private ViewExamRepository viewExamRepository;
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private Fixtures fixtures;

  private User user;
  private Lecture lecture;
  private String accessToken;

  @BeforeEach
  public void setup() {
    user = fixtures.유저_생성();
    lecture = fixtures.강의_생성();
    accessToken = fixtures.토큰_생성(user);
  }

  @Nested
  class 시험_평가_작성_테스트 {
    private final int WRITE_EXAM_POST_BONUS_POINT = 20;

    private final String endpoint = "/exam-posts";
    private final String paramKey = "lectureId";

    @Test
    void 시험_평가_작성_성공() throws Exception {
      // given
      var semester = lecture.getSemester().split(",")[0];
      var request = new ExamPostRequest.Create(lecture.getName(), semester, lecture.getProfessor(), "중간고사", "PPT", "어려움", "어렵네요");

      // when
      var result = post(Uri.of(endpoint), accessToken, request, parameter(paramKey, lecture.getId()));

      // then
      result.andExpect(status().isOk());

      var savedUser = userRepository.findById(user.getId()).orElseThrow();
      var examPosts = examPostRepository.findAllByUserIdx(user.getId());
      assertAll(
        () -> assertThat(examPosts).isNotEmpty().hasSize(1),
        () -> assertThat(examPosts.get(0).getUserIdx()).isEqualTo(user.getId()),
        () -> assertThat(examPosts.get(0).getExamDifficulty()).isEqualTo(request.getExamDifficulty()),
        () -> assertThat(examPosts.get(0).getExamInfo()).isEqualTo(request.getExamInfo()),
        () -> assertThat(examPosts.get(0).getExamType()).isEqualTo(request.getExamType()),
        () -> assertThat(examPosts.get(0).getContent()).isEqualTo(request.getContent()),
        () -> assertThat(examPosts.get(0).getLectureInfo().getLectureId()).isEqualTo(lecture.getId()),
        () -> assertThat(examPosts.get(0).getLectureName()).isEqualTo(request.getLectureName()),
        () -> assertThat(examPosts.get(0).getSelectedSemester()).isEqualTo(request.getSelectedSemester()),
        () -> assertThat(examPosts.get(0).getProfessor()).isEqualTo(request.getProfessor()),
        () -> assertThat(savedUser.getPoint()).isEqualTo(user.getPoint() + WRITE_EXAM_POST_BONUS_POINT),
        () -> assertThat(savedUser.getWrittenExam()).isEqualTo(user.getWrittenExam() + 1)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("create-exam-post")
          .summary("[토큰 필요] 시험 평가 생성 API")
          .description("시험 평가를 생성하는 API 입니다.")
          .tag(EXAM_POST)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 시험_평가_작성_실패_중복_작성() throws Exception {
      // given
      var examPost = fixtures.시험평가_생성(user.getId(), lecture);

      var request = new ExamPostRequest.Create(examPost.getLectureName(), examPost.getSelectedSemester(), examPost.getProfessor(), "중간고사", "PPT", "어려움", "어렵네요");

      // when
      var result = post(Uri.of(endpoint), accessToken, request, parameter(paramKey, lecture.getId()));

      // then
      result.andExpectAll(
        status().isBadRequest(),
        expectExceptionJsonPath(result, ALREADY_WROTE_EXAM_POST)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("create-exam-post-fail-already-written")
          .tag(EXAM_POST)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 시험_평가_생성_실패_존재하지_않는_강의() throws Exception {
      // given
      var request = new ExamPostRequest.Create("강의명", "2021-1", "교수", "중간고사", "PPT", "어려움", "어렵네요");

      // when
      var result = post(Uri.of(endpoint), accessToken, request, parameter(paramKey, -1L));

      // then
      result.andExpectAll(
        status().isNotFound(),
        expectExceptionJsonPath(result, LECTURE_NOT_FOUND)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("create-exam-post-fail-wrong-parameter-lecture-id")
          .tag(EXAM_POST)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Nested
  class 시험_평가_수정_테스트 {
    private final String endpoint = "/exam-posts";
    private final String paramKey = "examIdx";

    @Test
    void 시험_평가_수정_성공() throws Exception {
      // given
      var examPost = fixtures.시험평가_생성(user.getId(), lecture);

      var request = new ExamPostRequest.Update("2024-1", "기말고사", "과제", "쉬움", "쉽네요");

      // when
      var result = put(Uri.of(endpoint), accessToken, request, parameter(paramKey, examPost.getId()));

      // then
      result.andExpect(status().isOk());

      var evaluatePosts = examPostRepository.findAllByUserIdx(user.getId());

      assertAll(
        () -> assertThat(evaluatePosts.get(0)).isNotNull(),
        () -> assertThat(evaluatePosts.get(0).getExamDifficulty()).isEqualTo(request.getExamDifficulty()),
        () -> assertThat(evaluatePosts.get(0).getExamInfo()).isEqualTo(request.getExamInfo()),
        () -> assertThat(evaluatePosts.get(0).getExamType()).isEqualTo(request.getExamType()),
        () -> assertThat(evaluatePosts.get(0).getContent()).isEqualTo(request.getContent()),
        () -> assertThat(evaluatePosts.get(0).getSelectedSemester()).isEqualTo(request.getSelectedSemester())
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("update-exam-post")
          .summary("[토큰 필요] 시험 평가 수정 API")
          .description("시험 평가를 수정하는 API 입니다.")
          .tag(EXAM_POST)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 시험_평가_수정_실패_존재하지_않는_파라미터() throws Exception {
      // given
      var request = new ExamPostRequest.Update("2024-1", "기말고사", "과제", "쉬움", "쉽네요");

      // when
      var result = put(Uri.of(endpoint), accessToken, request, parameter(paramKey, -1L));

      // then
      result.andExpectAll(
        status().isNotFound(),
        expectExceptionJsonPath(result, EXAM_POST_NOT_FOUND)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("update-exam-post-fail-wrong-parameter-exam-id")
          .tag(EXAM_POST)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 시험_평가_수정_실패_작성자가_아님() throws Exception {
      // given
      var examPost = fixtures.시험평가_생성(user.getId(), lecture);
      var anotherUser = fixtures.다른_사용자_토큰_생성();

      var request = new ExamPostRequest.Update("2024-1", "기말고사", "과제", "쉬움", "쉽네요");

      // when
      var result = put(Uri.of(endpoint), anotherUser, request, parameter(paramKey, examPost.getId()));

      // then
      result.andExpectAll(
        status().is4xxClientError(),
        expectExceptionJsonPath(result, NOT_AN_AUTHOR)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("update-exam-post-fail-not-author")
          .tag(EXAM_POST)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Nested
  class 시험_평가_삭제_테스트 {
    private final String urlTemplate = "/exam-posts";
    private final String paramKey = "examIdx";

    @Test
    void 시험_평가_삭제_성공() throws Exception {
      // given
      var examPost = fixtures.시험평가_생성(user.getId(), lecture);

      fixtures.포인트_충전(user.getId());

      // when
      var result = delete(Uri.of(urlTemplate), accessToken, null, parameter(paramKey, examPost.getId()));

      // then
      result.andExpect(status().isOk());

      var examPosts = examPostRepository.findAllByUserIdx(user.getId());
      assertThat(examPosts).isEmpty();

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("delete-exam-post")
          .summary("[토큰 필요] 시험 평가 삭제 API")
          .description("시험 평가를 삭제하는 API 입니다.")
          .tag(EXAM_POST)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 시험_평가_삭제_실패_유저_포인트_부족() throws Exception {
      // given
      var examPost = fixtures.시험평가_생성(user.getId(), lecture);

      // when
      var result = delete(Uri.of(urlTemplate), accessToken, null, parameter(paramKey, examPost.getId()));

      // then
      result.andExpectAll(
        status().isBadRequest(),
        expectExceptionJsonPath(result, USER_POINT_LACK)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("delete-exam-post-fail-lack-user-point")
          .tag(EXAM_POST)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 시험_평가_삭제_실패_존재하지_않는_파라미터() throws Exception {
      // given

      // when
      var result = delete(Uri.of(urlTemplate), accessToken, null, parameter(paramKey, -1L));

      // then
      result.andExpectAll(
        status().isNotFound(),
        expectExceptionJsonPath(result, EXAM_POST_NOT_FOUND)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("delete-exam-post-fail-wrong-parameter-exam-id")
          .tag(EXAM_POST)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 시험_평가_삭제_실패_작성자가_아님() throws Exception {
      // given
      var examPost = fixtures.시험평가_생성(user.getId(), lecture);
      var anotherUser = fixtures.다른_사용자_토큰_생성();

      // when
      var result = delete(Uri.of(urlTemplate), anotherUser, null, parameter(paramKey, examPost.getId()));

      // then
      result.andExpectAll(
        status().is4xxClientError(),
        expectExceptionJsonPath(result, NOT_AN_AUTHOR)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("delete-exam-post-fail-not-author")
          .tag(EXAM_POST)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Nested
  class 시험_평가_구매_테스트 {
    private final int PURCHASE_POINT = 20;

    private final String endpoint = "/exam-posts/purchase";
    private final String paramKey = "lectureId";

    @Test
    void 시험_평가_구매_성공() throws Exception {
      // given
      fixtures.포인트_충전(user.getId());
      var before = userRepository.findById(user.getId()).orElseThrow();

      // when
      var result = post(Uri.of(endpoint), accessToken, null, parameter(paramKey, lecture.getId()));

      // then
      result.andExpect(status().isOk());

      var exists = viewExamRepository.isExists(user.getId(), lecture.getId());
      var after = userRepository.findById(user.getId()).orElseThrow();

      assertAll(
        () -> assertThat(exists).isTrue(),
        () -> assertThat(after.getPoint()).isEqualTo(before.getPoint() - PURCHASE_POINT),
        () -> assertThat(after.getViewExamCount()).isEqualTo(before.getViewExamCount() + 1)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("purchase-exam-post")
          .summary("[토큰 필요] 시험 평가 구매 API")
          .description("시험 평가를 구매하는 API 입니다.")
          .tag(EXAM_POST)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 시험_평가_구매_실패_유저_포인트_부족() throws Exception {
      // given

      // when
      var result = post(Uri.of(endpoint), accessToken, null, parameter(paramKey, lecture.getId()));

      // then
      result.andExpectAll(
        status().isBadRequest(),
        expectExceptionJsonPath(result, USER_POINT_LACK)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("purchase-exam-post-fail-lack-user-point")
          .tag(EXAM_POST)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 시험_평가_구매_실패_중복_구매() throws Exception {
      // given
      var viewExam = fixtures.시험평가_구매이력_생성(user.getId(), lecture.getId());

      // when
      var result = post(Uri.of(endpoint), accessToken, null, parameter(paramKey, viewExam.getLectureId()));

      // then
      result.andExpectAll(
        status().isBadRequest(),
        expectExceptionJsonPath(result, EXAM_POST_ALREADY_PURCHASE)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("purchase-exam-post-fail-already-purchase")
          .tag(EXAM_POST)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Test
  void 내가_쓴_시험_평가_조회_성공() throws Exception {
    // given
    final int DEFAULT_SIZE = 10;

    var totalPage = 3;
    var requestPage = 1;
    var size = DEFAULT_SIZE * totalPage;

    var examPosts = fixtures.시험평가_여러개_생성(user.getId(), lecture, size);

    // when
    var result = get(Uri.of("/exam-posts/written"), accessToken, parameter("page", requestPage));

    // then
    result.andExpectAll(
      status().isOk(),
      jsonPath("$.data.length()").value(1),
      jsonPath("$.data.[0].content").value(examPosts.get(0).getContent()),
      jsonPath("$.data.[0].lectureName").value(examPosts.get(0).getLectureName()),
      jsonPath("$.data.[0].selectedSemester").value(examPosts.get(0).getSelectedSemester()),
      jsonPath("$.data.[0].professor").value(lecture.getProfessor()),
      jsonPath("$.data.[0].majorType").value(lecture.getMajorType()),
      jsonPath("$.data.[0].semesterList").value(lecture.getSemester()),
      jsonPath("$.data.[0].examType").value(examPosts.get(0).getExamType()),
      jsonPath("$.data.[0].examInfo").value(examPosts.get(0).getExamInfo()),
      jsonPath("$.data.[0].examDifficulty").value(examPosts.get(0).getExamDifficulty())
    );

    // docs
    result.andDo(
      RestDocument.builder()
        .identifier("get-exam_post-written-by-me-success")
        .summary("내가 쓴 시험 평가 조회 API")
        .description("")
        .tag(EXAM_POST)
        .result(result)
        .generateDocs()
    );
  }

  @Nested
  class 시험_평가_구매_이력_조회_테스트 {
    @Test
    void 시험_평가_구매_이력_조회_성공() throws Exception {
      // given
      var viewExam = fixtures.시험평가_구매이력_생성(user.getId(), lecture.getId());

      // when
      var result = get(Uri.of("/exam-posts/purchase"), accessToken);

      // then
      result.andExpectAll(
        status().isOk(),
        jsonPath("$.data.[0].id").value(viewExam.getId()),
        jsonPath("$.data.[0].professor").value(lecture.getProfessor()),
        jsonPath("$.data.[0].lectureName").value(lecture.getName()),
        jsonPath("$.data.[0].majorType").value(lecture.getMajorType()),
        jsonPath("$.data.[0].createDate").value(viewExam.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("get-exam_post-purchase-history-success")
          .summary("[토큰 필요] 시험 평가 구매 이력 조회 API")
          .description("")
          .tag(EXAM_POST)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Nested
  class 시험_평가_리스트_조회_테스트 {
    private static final int DEFAULT_SIZE = 10;

    private final int totalPage = 3;
    private final int requestPage = 1;
    private final int size = DEFAULT_SIZE * totalPage;

    private final String urlTemplate = "/exam-posts";
    private final String pageParam = "page";
    private final String lectureIdParam = "lectureId";

    @Test
    void 시험_평가_리스트_조회_성공_시험_평가_미작성_유저() throws Exception {
      // given
      fixtures.시험평가_구매이력_생성(user.getId(), lecture.getId());

      var examPosts = fixtures.시험평가_여러개_생성_유저_제외(user.getId(), lecture, size);

      // when
      var result = get(Uri.of(urlTemplate), accessToken,
        parameter(pageParam, requestPage),
        parameter(lectureIdParam, lecture.getId())
      );

      // then
      result.andExpectAll(
        status().isOk(),
        jsonPath("$.data.size()").value(DEFAULT_SIZE),
        jsonPath("$.data.[0].id").value(examPosts.get(0).getId()),
        jsonPath("$.data.[0].content").value(examPosts.get(0).getContent()),
        jsonPath("$.data.[0].selectedSemester").value(examPosts.get(0).getSelectedSemester()),
        jsonPath("$.data.[0].examType").value(examPosts.get(0).getExamType()),
        jsonPath("$.data.[0]..examInfo").value(examPosts.get(0).getExamInfo()),
        jsonPath("$.data.[0]..examDifficulty").value(examPosts.get(0).getExamDifficulty()),
        jsonPath("$.canRead").value(true),
        jsonPath("$.examDataExist").value(true),
        jsonPath("$.written").value(false)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("get-exam_posts-success")
          .summary("시험 평가 리스트 조회 API")
          .description("""
            파라미터는 다음과 같습니다.
            page : 정수,
            lectureId : 강의 ID
            """)
          .tag(EXAM_POST)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 시험_평가_리스트_조회_성공_시험_평가_작성_유저() throws Exception {
      // given
      fixtures.시험평가_구매이력_생성(user.getId(), lecture.getId());
      var examPosts = fixtures.시험평가_여러개_생성(user.getId(), lecture, size);

      // when
      var result = get(Uri.of(urlTemplate), accessToken,
        parameter(pageParam, requestPage),
        parameter(lectureIdParam, lecture.getId())
      );

      // then
      result.andExpectAll(
        status().isOk(),
        jsonPath("$.data.size()").value(DEFAULT_SIZE),
        jsonPath("$.data.[0].id").value(examPosts.get(0).getId()),
        jsonPath("$.data.[0].content").value(examPosts.get(0).getContent()),
        jsonPath("$.data.[0].selectedSemester").value(examPosts.get(0).getSelectedSemester()),
        jsonPath("$.data.[0].examType").value(examPosts.get(0).getExamType()),
        jsonPath("$.data.[0]..examInfo").value(examPosts.get(0).getExamInfo()),
        jsonPath("$.data.[0]..examDifficulty").value(examPosts.get(0).getExamDifficulty()),
        jsonPath("$.canRead").value(true),
        jsonPath("$.examDataExist").value(true),
        jsonPath("$.written").value(true)
      );
    }

    @Test
    void 시험_평가_리스트_조회_성공_미구매_유저() throws Exception {
      // given
      var examPosts = fixtures.시험평가_여러개_생성(user.getId(), lecture, size);

      // when
      var result = get(Uri.of(urlTemplate), accessToken,
        parameter(pageParam, requestPage),
        parameter(lectureIdParam, lecture.getId())
      );

      // then
      result.andExpectAll(
        status().isOk(),
        jsonPath("$.data").isEmpty(),
        jsonPath("$.canRead").value(false),
        jsonPath("$.examDataExist").value(true),
        jsonPath("$.written").value(true)
      );
    }
  }
}

package usw.suwiki.api.evaluate;

import io.github.hejow.restdocs.document.RestDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import usw.suwiki.common.test.Tag;
import usw.suwiki.common.test.annotation.AcceptanceTest;
import usw.suwiki.common.test.support.AcceptanceTestSupport;
import usw.suwiki.common.test.support.ResponseValidator;
import usw.suwiki.common.test.support.Uri;
import usw.suwiki.domain.evaluatepost.EvaluatePostRepository;
import usw.suwiki.domain.evaluatepost.dto.EvaluatePostRequest;
import usw.suwiki.domain.lecture.Lecture;
import usw.suwiki.domain.user.User;
import usw.suwiki.test.fixture.Fixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static usw.suwiki.common.test.Tag.EVALUATE_POST;
import static usw.suwiki.common.test.extension.AssertExtension.expectExceptionJsonPath;
import static usw.suwiki.common.test.support.Pair.parameter;
import static usw.suwiki.core.exception.ExceptionType.ALREADY_WROTE_EXAM_POST;
import static usw.suwiki.core.exception.ExceptionType.EVALUATE_POST_NOT_FOUND;
import static usw.suwiki.core.exception.ExceptionType.LECTURE_NOT_FOUND;
import static usw.suwiki.core.exception.ExceptionType.PARAMETER_VALIDATION_FAIL;
import static usw.suwiki.core.exception.ExceptionType.USER_POINT_LACK;

@AcceptanceTest
public class EvaluatePostAcceptanceTest extends AcceptanceTestSupport {
  @Autowired
  private EvaluatePostRepository evaluatePostRepository;

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
  class 강의_평가_생성_테스트 {
    private final String endpoint = "/evaluate-posts";
    private final String paramKey = "lectureId";

    @Test
    void 강의_평가_생성_성공() throws Exception {
      // expected
      var identifier = "create-evaluate-post";
      var summary = "[토큰 필요] 강의 평가 생성 API";
      var description = "강의 평가를 생성하는 API 입니다. Body에는 String 타입을 입력해야하며 Blank 제약조건이 있습니다."; // todo
      var expectedResults = "success";

      // given
      var request = new EvaluatePostRequest.Create("강의 평가 내용", "강의명", "2021-1", "교수명", 1, 1, 1, 1, 1, 1);

      // when
      var result = post(Uri.of(endpoint), accessToken, request, parameter(paramKey, lecture.getId()));

      // then
      var evaluatePosts = evaluatePostRepository.findAllByUserIdx(user.getId());
      assertAll(
        () -> assertThat(evaluatePosts.get(0)).isNotNull(),
        () -> assertThat(evaluatePosts.get(0).getContent()).isEqualTo(request.getContent()),
        () -> assertThat(evaluatePosts.get(0).getLectureName()).isEqualTo(request.getLectureName()),
        () -> assertThat(evaluatePosts.get(0).getLectureInfo().getSelectedSemester()).isEqualTo(request.getSelectedSemester()),
        () -> assertThat(evaluatePosts.get(0).getLectureInfo().getProfessor()).isEqualTo(request.getProfessor()),
        () -> assertThat(evaluatePosts.get(0).getLectureRating().getSatisfaction()).isEqualTo(request.getSatisfaction()),
        () -> assertThat(evaluatePosts.get(0).getLectureRating().getLearning()).isEqualTo(request.getLearning()),
        () -> assertThat(evaluatePosts.get(0).getLectureRating().getHoney()).isEqualTo(request.getHoney()),
        () -> assertThat(evaluatePosts.get(0).getLectureRating().getTeam()).isEqualTo(request.getTeam()),
        () -> assertThat(evaluatePosts.get(0).getLectureRating().getDifficulty()).isEqualTo(request.getDifficulty()),
        () -> assertThat(evaluatePosts.get(0).getLectureRating().getHomework()).isEqualTo(request.getHomework())
      );

      // result validation
      ResponseValidator.validateHtml(result, status().isOk(), expectedResults);

      // Non DOCS
    }

    @ParameterizedTest
    @ValueSource(floats = {-1, -100})
    void 강의_평가_생성_실패_잘못된_만족도(float negative) throws Exception {
      // given
      var request = new EvaluatePostRequest.Create("강의 평가 내용", "강의명", "2021-1", "교수명", negative, 1, 1, 1, 1, 1);

      // when
      var result = post(Uri.of(endpoint), accessToken, request, parameter(paramKey, lecture.getId()));

      // then
      result.andExpectAll(
        status().isBadRequest(),
        expectExceptionJsonPath(result, PARAMETER_VALIDATION_FAIL)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("create-evaluate-post-fail-wrong-satisfaction-value")
          .tag(Tag.EVALUATE_POST)
          .result(result)
          .generateDocs()
      );
    }

    @ParameterizedTest
    @ValueSource(floats = {-1, -100})
    void 강의_평가_생성_실패_잘못된_학습도(float negative) throws Exception {
      // given
      var request = new EvaluatePostRequest.Create("강의 평가 내용", "강의명", "2021-1", "교수명", 1, negative, 1, 1, 1, 1);

      // when
      var result = post(Uri.of(endpoint), accessToken, request, parameter(paramKey, lecture.getId()));

      // then
      result.andExpectAll(
        status().isBadRequest(),
        expectExceptionJsonPath(result, PARAMETER_VALIDATION_FAIL)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("create-evaluate-post-fail-wrong-learning-value")
          .tag(Tag.EVALUATE_POST)
          .result(result)
          .generateDocs()
      );
    }

    @ParameterizedTest
    @ValueSource(floats = {-1, -100})
    void 강의_평가_생성_실패_잘못된_꿀강지수(float negative) throws Exception {
      // given
      var request = new EvaluatePostRequest.Create("강의 평가 내용", "강의명", "2021-1", "교수명", 1, 1, negative, 1, 1, 1);

      // when
      var result = post(Uri.of(endpoint), accessToken, request, parameter(paramKey, lecture.getId()));

      // then
      result.andExpectAll(
        status().isBadRequest(),
        expectExceptionJsonPath(result, PARAMETER_VALIDATION_FAIL)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("create-evaluate-post-fail-wrong-honey-value")
          .tag(Tag.EVALUATE_POST)
          .result(result)
          .generateDocs()
      );
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100})
    void 강의_평가_생성_실패_잘못된_팀플지수(int negative) throws Exception {
      // given
      var request = new EvaluatePostRequest.Create(
        "강의 평가 내용", "강의명", "2021-1", "교수명",
        1, 1, 1, negative, 1, 1
      );

      // when
      var result = post(Uri.of(endpoint), accessToken, request, parameter(paramKey, lecture.getId()));

      // then
      result.andExpectAll(
        status().isBadRequest(),
        expectExceptionJsonPath(result, PARAMETER_VALIDATION_FAIL)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("create-evaluate-post-fail-wrong-team-value")
          .tag(Tag.EVALUATE_POST)
          .result(result)
          .generateDocs()
      );
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100})
    void 강의_평가_생성_실패_잘못된_어려움지수(int negative) throws Exception {
      // given
      var request = new EvaluatePostRequest.Create("강의 평가 내용", "강의명", "2021-1", "교수명", 1, 1, 1, 1, negative, 1);

      // when
      var result = post(Uri.of(endpoint), accessToken, request, parameter(paramKey, lecture.getId()));

      // then
      result.andExpectAll(
        status().isBadRequest(),
        expectExceptionJsonPath(result, PARAMETER_VALIDATION_FAIL)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("create-evaluate-post-fail-wrong-difficult-value")
          .tag(Tag.EVALUATE_POST)
          .result(result)
          .generateDocs()
      );
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100})
    void 강의_평가_생성_실패_잘못된_과제지수(int negative) throws Exception {
      // given
      var request = new EvaluatePostRequest.Create("강의 평가 내용", "강의명", "2021-1", "교수명", 1, 1, 1, 1, 1, negative);

      // when
      var result = post(Uri.of(endpoint), accessToken, request, parameter("lectureId", lecture.getId()));

      // then
      result.andExpectAll(
        status().isBadRequest(),
        expectExceptionJsonPath(result, PARAMETER_VALIDATION_FAIL)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("create-evaluate-post-fail-wrong-homework-value")
          .tag(Tag.EVALUATE_POST)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 강의_평가_생성_실패_존재하지_않는_강의() throws Exception {
      // given
      var request = new EvaluatePostRequest.Create("강의 평가 내용", "강의명", "2021-1", "교수명", 1, 1, 1, 1, 1, 1);

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
          .identifier("create-evaluate-post-fail-wrong-parameter-lecture-id")
          .tag(Tag.EVALUATE_POST)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 강의_평가_생성_실패_중복_작성() throws Exception {
      // given
      var evaluatePost = fixtures.강의평가_생성(user.getId(), lecture);

      var request = new EvaluatePostRequest.Create("강의 평가 내용", "강의명", "2021-1", "교수명", 1, 1, 1, 1, 1, 1);

      // when
      var result = post(Uri.of(endpoint), accessToken, request, parameter(paramKey, evaluatePost.getLectureId()));

      // then
      result.andExpectAll(
        status().isBadRequest(),
        expectExceptionJsonPath(result, ALREADY_WROTE_EXAM_POST)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("create-evaluate-post-fail-already-write-evaluate-post")
          .tag(Tag.EVALUATE_POST)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Nested
  class 강의_평가_수정_테스트 {
    private final String endpoint = "/evaluate-posts";
    private final String paramKey = "evaluateIdx";

    @Test
    void 강의_평가_수정_성공() throws Exception {
      // expected
      var identifier = "update-evaluate-post";
      var summary = "[토큰 필요] 강의 평가 수정 API";
      var description = "강의 평가를 수정하는 API 입니다";
      var expectedResults = "success";

      // given
      var evaluatePost = fixtures.강의평가_생성(user.getId(), lecture);

      var request = new EvaluatePostRequest.Update("바뀐 강의 평가 내용", "2021-2", 2, 2, 2, 2, 2, 2);

      // when
      var result = put(Uri.of(endpoint), accessToken, request, parameter(paramKey, evaluatePost.getId()));

      // then
      var evaluatePosts = evaluatePostRepository.findAllByUserIdx(user.getId());
      assertAll(
        () -> assertThat(evaluatePosts.get(0)).isNotNull(),
        () -> assertThat(evaluatePosts.get(0).getContent()).isEqualTo(request.getContent()),
        () -> assertThat(evaluatePosts.get(0).getLectureInfo().getSelectedSemester()).isEqualTo(request.getSelectedSemester()),
        () -> assertThat(evaluatePosts.get(0).getLectureRating().getSatisfaction()).isEqualTo(request.getSatisfaction()),
        () -> assertThat(evaluatePosts.get(0).getLectureRating().getLearning()).isEqualTo(request.getLearning()),
        () -> assertThat(evaluatePosts.get(0).getLectureRating().getHoney()).isEqualTo(request.getHoney()),
        () -> assertThat(evaluatePosts.get(0).getLectureRating().getTeam()).isEqualTo(request.getTeam()),
        () -> assertThat(evaluatePosts.get(0).getLectureRating().getDifficulty()).isEqualTo(request.getDifficulty()),
        () -> assertThat(evaluatePosts.get(0).getLectureRating().getHomework()).isEqualTo(request.getHomework())
      );

      // result validation
      ResponseValidator.validateHtml(result, status().isOk(), expectedResults);

      //Non DOCS
    }

    @Test
    void 강의_평가_수정_실패_존재하지_않는_강의_평가() throws Exception {
      // given
      var request = new EvaluatePostRequest.Update("바뀐 강의 평가 내용", "2021-2", 2, 2, 2, 2, 2, 2);

      // when
      var result = put(Uri.of(endpoint), accessToken, request, parameter(paramKey, -1L));

      // then
      result.andExpectAll(
        status().isNotFound(),
        expectExceptionJsonPath(result, EVALUATE_POST_NOT_FOUND)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("update-evaluate-post-fail-wrong-parameter-evaluate-id")
          .tag(Tag.EVALUATE_POST)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Nested
  class 강의_평가_삭제_테스트 {
    private final String endpoint = "/evaluate-posts";
    private final String paramKey = "evaluateIdx";

    @Test
    void 강의_평가_삭제_성공() throws Exception {
      // expected
      var identifier = "delete-evaluate-post";
      var summary = "[토큰 필요] 강의 평가 삭제 API";
      var description = "강의 평가를 삭제하는 API 입니다";
      var expectedResults = "success";

      // given
      var evaluatePost = fixtures.강의평가_생성(user.getId(), lecture);

      for (int cnt = 0; cnt < 10; cnt++) { // 삭제 시 필요한 포인트 30 포인트 차감. 한번 작성 시 10포인트 증가
        user.writeEvaluatePost();
      }

      // when
      var result = delete(Uri.of(endpoint), accessToken, null, parameter(paramKey, evaluatePost.getId()));

      // then
      var evaluatePosts = evaluatePostRepository.findAllByUserIdx(user.getId());
      assertThat(evaluatePosts).isEmpty();

      // result validation
      ResponseValidator.validateHtml(result, status().isOk(), expectedResults);
    }

    @Test
    void 강의_평가_삭제_실패_유저_포인트_부족() throws Exception {
      // given
      var evaluatePost = fixtures.강의평가_생성(user.getId(), lecture);

      // when
      var result = delete(Uri.of(endpoint), accessToken, null, parameter(paramKey, evaluatePost.getId()));

      // then
      result.andExpectAll(
        status().isBadRequest(),
        expectExceptionJsonPath(result, USER_POINT_LACK)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("delete-evaluate-post-fail-lack-user-point")
          .tag(Tag.EVALUATE_POST)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Nested
  class 강의_평가_리스트_조회_테스트 {
    private static final int DEFAULT_SIZE = 10;

    private final int totalPage = 3;
    private final int requestPage = 1;
    private final int size = DEFAULT_SIZE * totalPage;

    private final String endpoint = "/evaluate-posts";
    private final String pageParam = "page";
    private final String lectureIdParam = "lectureId";

    @Test
    void 강의_평가_리스트_조회_성공() throws Exception {
      // given
      var evaluatePosts = fixtures.강의평가_여러개_생성(user.getId(), lecture, size);

      // when
      var result = get(Uri.of(endpoint), accessToken,
        parameter(pageParam, requestPage),
        parameter(lectureIdParam, lecture.getId())
      );

      // then
      result.andExpectAll(
        status().isOk(),
        jsonPath("$.data.size()").value(DEFAULT_SIZE),
        jsonPath("$.data.[0].id").value(evaluatePosts.get(0).getId()),
        jsonPath("$.data.[0].content").value(evaluatePosts.get(0).getContent()),
        jsonPath("$.data.[0].selectedSemester").value(evaluatePosts.get(0).getSelectedSemester()),
        jsonPath("$.data.[0].totalAvg").value(evaluatePosts.get(0).getTotalAvg()),
        jsonPath("$.data.[0].satisfaction").value(evaluatePosts.get(0).getSatisfaction()),
        jsonPath("$.data.[0].learning").value(evaluatePosts.get(0).getLearning()),
        jsonPath("$.data.[0].honey").value(evaluatePosts.get(0).getHoney()),
        jsonPath("$.data.[0].team").value(evaluatePosts.get(0).getTeam()),
        jsonPath("$.data.[0].difficulty").value(evaluatePosts.get(0).getDifficulty()),
        jsonPath("$.data.[0].homework").value(evaluatePosts.get(0).getHomework()),
        jsonPath("$.written").value(false)
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("get-evaluate-posts-success")
          .summary("[토큰 필요] 강의 평가 리스트 조회 API")
          .description("강의 평가 조회 API입니다.")
          .tag(EVALUATE_POST)
          .result(result)
          .generateDocs()
      );
    }
  }
}

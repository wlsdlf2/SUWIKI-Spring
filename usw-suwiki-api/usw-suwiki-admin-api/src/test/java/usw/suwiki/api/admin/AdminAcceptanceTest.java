package usw.suwiki.api.admin;

import io.github.hejow.restdocs.generator.RestDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import usw.suwiki.common.test.annotation.AcceptanceTest;
import usw.suwiki.common.test.fixture.Fixtures;
import usw.suwiki.common.test.support.AcceptanceTestSupport;
import usw.suwiki.common.test.support.Uri;
import usw.suwiki.core.secure.Encoder;
import usw.suwiki.domain.evaluatepost.EvaluatePostRepository;
import usw.suwiki.domain.exampost.ExamPostRepository;
import usw.suwiki.domain.report.EvaluateReportRepository;
import usw.suwiki.domain.report.ExamReportRepository;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.UserRepository;
import usw.suwiki.domain.user.blacklist.BlacklistRepository;
import usw.suwiki.domain.user.dto.AdminRequest;
import usw.suwiki.domain.user.dto.UserRequest;
import usw.suwiki.domain.user.restricted.RestrictingUserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static usw.suwiki.common.test.Tag.REPORT;
import static usw.suwiki.common.test.Tag.USER;
import static usw.suwiki.common.test.support.ResponseValidator.validate;
import static usw.suwiki.core.exception.ExceptionType.PARAMETER_VALIDATION_FAIL;
import static usw.suwiki.core.exception.ExceptionType.PASSWORD_ERROR;
import static usw.suwiki.core.exception.ExceptionType.USER_RESTRICTED;

@AcceptanceTest
class AdminAcceptanceTest extends AcceptanceTestSupport {
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private BlacklistRepository blacklistRepository;
  @Autowired
  private RestrictingUserRepository restrictingUserRepository;

  @Autowired
  private EvaluatePostRepository evaluatePostRepository;
  @Autowired
  private ExamPostRepository examPostRepository;

  @Autowired
  private EvaluateReportRepository evaluateReportRepository;
  @Autowired
  private ExamReportRepository examReportRepository;

  @Autowired
  private Encoder encoder;
  @Autowired
  private Fixtures fixtures;

  private static final String loginId = "admin";
  private static final String password = "password";

  private User admin;
  private String accessToken;

  @BeforeEach
  public void setup() {
    admin = fixtures.관리자_생성(loginId, encoder.encode(password));
    accessToken = fixtures.토큰_생성(admin);
  }

  @Nested
  class 관리자_로그인_테스트 {
    private final String endpoint = "/admin/login";

    @Test
    void 로그인_성공() throws Exception {
      // given
      var request = new UserRequest.Login(loginId, password);

      // when
      var result = post(Uri.of(endpoint), request);

      // then
      result.andExpectAll(
        status().isOk(),
        jsonPath("$.accessToken").exists(),
        jsonPath("$.userCount").exists()
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("관리자 로그인 API")
          .description("""
            관리자 로그인 API 입니다.
            Refresh Token 을 따로 내려주지 않으며, 화면에 필요한 총 유저 수를 제공합니다.
            """)
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 로그인_실패_비밀번호_불일치() throws Exception {
      // given
      var request = new UserRequest.Login(loginId, "wrongPassword");

      // when
      var result = post(Uri.of(endpoint), request);

      // then
      validate(result, status().isBadRequest(), PASSWORD_ERROR);

      // docs
      result.andDo(
        RestDocument.builder()
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 로그인_실패_일반_유저() throws Exception {
      // given
      fixtures.유저_생성("user", encoder.encode(password));

      var request = new UserRequest.Login("user", password);

      // when
      var result = post(Uri.of(endpoint), request);

      // then
      validate(result, status().isForbidden(), USER_RESTRICTED);

      // docs
      result.andDo(
        RestDocument.builder()
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 로그인_실패_잘못된_파라미터(String parameter) throws Exception {
      // given
      var request = new UserRequest.Login(parameter, parameter);

      // when
      var result = post(Uri.of(endpoint), request);

      // then
      validate(result, status().isBadRequest(), PARAMETER_VALIDATION_FAIL);

      // docs
      result.andDo(
        RestDocument.builder()
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Nested
  class 강의평가_신고_제재_테스트 {
    private final String endpoint = "/admin/restrict/evaluate-posts";

    @Test
    void 강의평가_신고_제재_처리_성공() throws Exception {
      // given
      var user = fixtures.유저_생성();
      var lecture = fixtures.강의_생성();
      var evaluatePost = fixtures.강의평가_생성(user.getId(), lecture);
      fixtures.강의평가_신고_생성(admin.getId(), evaluatePost);

      var request = new AdminRequest.RestrictEvaluatePost(evaluatePost.getId(), 30L, "그냥", "사형");

      var beforePoint = admin.getPoint();
      var beforeRestrictedCount = user.getRestrictedCount();

      // when
      var result = post(Uri.of(endpoint), accessToken, request);

      // then
      result.andExpect(status().isOk());

      var reporter = userRepository.findById(admin.getId()).orElseThrow();
      var reported = userRepository.findById(user.getId()).orElseThrow();

      var blacklists = blacklistRepository.findAll();
      var restrictingUsers = restrictingUserRepository.findAll();

      var evaluatePosts = evaluatePostRepository.findAll();
      var evaluatePostReports = evaluateReportRepository.findAll();

      assertAll(
        () -> assertThat(evaluatePostReports).isEmpty(),
        () -> assertThat(evaluatePosts).isEmpty(),
        () -> assertThat(reporter.getPoint()).isEqualTo(beforePoint + 1),
        () -> assertThat(reported.getRestrictedCount()).isEqualTo(beforeRestrictedCount + 1),
        () -> assertThat(reported.isRestricted()).isTrue(),
        () -> assertThat(restrictingUsers).isNotEmpty().hasSize(1),
        () -> assertThat(blacklists).isEmpty()
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("[토큰 필요] 강의평가 신고 제재 처리 API")
          .description("신고된 강의평가를 제재하는 API 입니다. ADMIN 계정이 아니라면 요청이 거부됩니다.")
          .tag(REPORT)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 강의평가_신고_제재_처리_성공_제재_누적으로_블랙리스트_전환() throws Exception {
      // given
      var user = fixtures.유저_생성();
      user.reported();
      user.reported();
      userRepository.save(user);

      var lecture = fixtures.강의_생성();
      var evaluatePost = fixtures.강의평가_생성(user.getId(), lecture);
      fixtures.강의평가_신고_생성(admin.getId(), evaluatePost);

      var request = new AdminRequest.RestrictEvaluatePost(evaluatePost.getId(), 30L, "그냥", "사형");

      var beforePoint = admin.getPoint();

      // when
      var result = post(Uri.of(endpoint), accessToken, request);

      // then
      result.andExpect(status().isOk());

      var reporter = userRepository.findById(admin.getId()).orElseThrow();

      var evaluatePosts = evaluatePostRepository.findAll();
      var evaluatePostReports = evaluateReportRepository.findAll();

      var blacklists = blacklistRepository.findAll();
      var restrictingUsers = restrictingUserRepository.findAll();

      assertAll(
        () -> assertThat(evaluatePostReports).isEmpty(),
        () -> assertThat(evaluatePosts).isEmpty(),
        () -> assertThat(reporter.getPoint()).isEqualTo(beforePoint + 1),
        () -> assertThat(blacklists).isNotEmpty().hasSize(1),
        () -> assertThat(restrictingUsers).isEmpty()
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .tag(REPORT)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 강의평가_신고_제재_실패_권한_없음() throws Exception {
      // given
      var userToken = fixtures.토큰_생성();

      var request = new AdminRequest.RestrictEvaluatePost(-1L, 30L, "그냥", "사형");

      // when
      var result = post(Uri.of(endpoint), userToken, request);

      // then
      validate(result, status().isForbidden(), USER_RESTRICTED);

      // docs
      result.andDo(
        RestDocument.builder()
          .tag(REPORT)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Nested
  class 시험평가_신고_제재_테스트 {
    private final String endpoint = "/admin/restrict/exam-post";

    @Test
    void 시험평가_신고_제재_처리_성공() throws Exception {
      // given
      var user = fixtures.유저_생성();
      var lecture = fixtures.강의_생성();
      var examPost = fixtures.시험평가_생성(user.getId(), lecture);
      fixtures.시험평가_신고_생성(admin.getId(), examPost);

      var request = new AdminRequest.RestrictExamPost(examPost.getId(), 30L, "그냥", "사형");

      var pointBefore = admin.getPoint();
      var restrictedCountBefore = user.getRestrictedCount();

      // when
      var result = post(Uri.of(endpoint), accessToken, request);

      // then
      result.andExpect(status().isOk());

      var reporter = userRepository.findById(admin.getId()).orElseThrow();
      var reported = userRepository.findById(user.getId()).orElseThrow();

      var blacklists = blacklistRepository.findAll();
      var restrictingUsers = restrictingUserRepository.findAll();

      var examPosts = examPostRepository.findAll();
      var examPostReports = examReportRepository.findAll();

      assertAll(
        () -> assertThat(examPosts).isEmpty(),
        () -> assertThat(examPostReports).isEmpty(),
        () -> assertThat(reporter.getPoint()).isEqualTo(pointBefore + 1),
        () -> assertThat(reported.getRestrictedCount()).isEqualTo(restrictedCountBefore + 1),
        () -> assertThat(reported.isRestricted()).isTrue(),
        () -> assertThat(restrictingUsers).isNotEmpty().hasSize(1),
        () -> assertThat(blacklists).isEmpty()
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("[토큰 필요] 시험평가 신고 제재 처리 API")
          .description("신고된 시험평가를 제재하는 API 입니다. ADMIN 계정이 아니라면 요청이 거부됩니다.")
          .tag(REPORT)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 시험평가_신고_제재_처리_성공_제재_누적으로_블랙리스트_전환() throws Exception {
      // given
      var user = fixtures.유저_생성();
      user.reported();
      user.reported();
      userRepository.save(user);

      var lecture = fixtures.강의_생성();
      var examPost = fixtures.시험평가_생성(user.getId(), lecture);
      fixtures.시험평가_신고_생성(admin.getId(), examPost);

      var request = new AdminRequest.RestrictExamPost(examPost.getId(), 30L, "그냥", "사형");

      var pointBefore = admin.getPoint();

      // when
      var result = post(Uri.of(endpoint), accessToken, request);

      // then
      result.andExpect(status().isOk());

      var reporter = userRepository.findById(admin.getId()).orElseThrow();

      var examPosts = examPostRepository.findAll();
      var examPostReports = examReportRepository.findAll();

      var blacklists = blacklistRepository.findAll();
      var restrictingUsers = restrictingUserRepository.findAll();

      assertAll(
        () -> assertThat(examPosts).isEmpty(),
        () -> assertThat(examPostReports).isEmpty(),
        () -> assertThat(reporter.getPoint()).isEqualTo(pointBefore + 1),
        () -> assertThat(blacklists).isNotEmpty().hasSize(1),
        () -> assertThat(restrictingUsers).isEmpty()
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .tag(REPORT)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 시험평가_신고_제재_실패_권한_없음() throws Exception {
      // given
      var userToken = fixtures.토큰_생성();

      var request = new AdminRequest.RestrictExamPost(-1L, 30L, "그냥", "사형");

      // when
      var result = post(Uri.of(endpoint), userToken, request);

      // then
      validate(result, status().isForbidden(), USER_RESTRICTED);

      // docs
      result.andDo(
        RestDocument.builder()
          .tag(REPORT)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Nested
  class 신고_블랙_등록_테스트 {

  }

}

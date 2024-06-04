package usw.suwiki.api.user;

import io.github.hejow.restdocs.generator.RestDocument;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.auth.token.ConfirmationToken;
import usw.suwiki.auth.token.ConfirmationTokenRepository;
import usw.suwiki.common.test.annotation.AcceptanceTest;
import usw.suwiki.common.test.support.AcceptanceTestSupport;
import usw.suwiki.common.test.support.Uri;
import usw.suwiki.core.secure.PasswordEncoder;
import usw.suwiki.domain.lecture.Major;
import usw.suwiki.domain.lecture.major.FavoriteMajorRepository;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.UserRepository;
import usw.suwiki.domain.user.dto.MajorRequest;
import usw.suwiki.domain.user.dto.UserRequest;
import usw.suwiki.domain.user.dto.UserRequest.CheckEmail;
import usw.suwiki.domain.user.dto.UserRequest.CheckLoginId;
import usw.suwiki.domain.user.dto.UserRequest.EditPassword;
import usw.suwiki.domain.user.dto.UserRequest.FindId;
import usw.suwiki.domain.user.dto.UserRequest.FindPassword;
import usw.suwiki.domain.user.dto.UserRequest.Join;
import usw.suwiki.test.fixture.Fixtures;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static usw.suwiki.auth.token.response.ConfirmResponse.ERROR;
import static usw.suwiki.auth.token.response.ConfirmResponse.EXPIRED;
import static usw.suwiki.auth.token.response.ConfirmResponse.SUCCESS;
import static usw.suwiki.common.test.Tag.USER;
import static usw.suwiki.common.test.support.Pair.parameter;
import static usw.suwiki.common.test.support.ResponseValidator.validate;
import static usw.suwiki.common.test.support.ResponseValidator.validateHtml;
import static usw.suwiki.core.exception.ExceptionType.EMAIL_NOT_AUTHED;
import static usw.suwiki.core.exception.ExceptionType.INVALID_EMAIL_FORMAT;
import static usw.suwiki.core.exception.ExceptionType.INVALID_TOKEN;
import static usw.suwiki.core.exception.ExceptionType.LOGIN_ID_OR_EMAIL_OVERLAP;
import static usw.suwiki.core.exception.ExceptionType.PARAMETER_VALIDATION_FAIL;
import static usw.suwiki.core.exception.ExceptionType.PASSWORD_ERROR;
import static usw.suwiki.core.exception.ExceptionType.SAME_PASSWORD_WITH_OLD;
import static usw.suwiki.core.exception.ExceptionType.USER_NOT_FOUND;

@AcceptanceTest
class UserAcceptanceTest extends AcceptanceTestSupport {
  private static final String loginId = "suwiki";
  private static final String email = "suwiki@suwon.ac.kr";
  private static final String password = "p@sSw0rc1!!";

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private FavoriteMajorRepository favoriteMajorRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private Fixtures fixtures;

  @SpyBean
  private ConfirmationTokenRepository confirmationTokenRepository;

  private User user;
  private String accessToken;

  @BeforeEach
  public void setup() {
    user = userRepository.save(User.join(loginId, passwordEncoder.encode(password), email).activate());
    accessToken = fixtures.토큰_생성(user);
  }

  @Nested
  class 아이디_중복_확인_테스트 {
    private final String endpoint = "/user/check-id";

    @Test
    void 아이디_중복확인_성공_중복() throws Exception {
      // setup
      var request = new CheckLoginId(loginId);

      // execution
      var result = post(Uri.of(endpoint), request);

      // result validation
      validate(result, status().isOk(), Pair.of("$.overlap", true));

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("아이디 중복 확인 API")
          .description("아이디 중복 확인 API입니다. Body에는 String 타입을 입력해야하며 Blank 제약조건이 있습니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 아이디_중복확인_성공_중복_아님() throws Exception {
      // setup
      var request = new CheckLoginId("diger");

      // execution
      var result = post(Uri.of(endpoint), request);

      // result validation
      validate(result, status().isOk(), Pair.of("$.overlap", false));

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
    void 아이디_중복확인_실패_잘못된_파라미터(String loginId) throws Exception {
      // setup
      var request = new CheckLoginId(loginId);

      // execution
      var result = post(Uri.of(endpoint), request);

      // result validation
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
  class 이메일_중복_확인_테스트 {
    private final String urlTemplate = "/user/check-email";

    @Test
    void 이메일_중복확인_성공_중복() throws Exception {
      // setup
      var request = new CheckEmail(email);

      // execution
      var result = post(Uri.of(urlTemplate), request);

      // result validation
      validate(result, status().isOk(), Pair.of("$.overlap", true));

      // docs
      result.andDo(
        RestDocument.builder()
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 이메일_중복확인_성공_중복_아님() throws Exception {
      // setup
      var request = new CheckEmail("diger@suwon.ac.kr");

      // execution
      var result = post(Uri.of(urlTemplate), request);

      // result validation
      validate(result, status().isOk(), Pair.of("$.overlap", false));

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("이메일 중복 확인 API")
          .description("이메일 중복 확인 API입니다. Body에는 String 타입을 입력해야하며 Blank 제약조건이 있습니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 아이디_중복확인_실패_잘못된_파라미터(String email) throws Exception {
      // setup
      var request = new CheckEmail(email);

      // execution
      var result = post(Uri.of(urlTemplate), request);

      // result validation
      validate(result, status().isBadRequest(), PARAMETER_VALIDATION_FAIL);

      result.andDo(
        RestDocument.builder()
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Nested
  class 회원가입_테스트 {
    private final String endpoint = "/user/join";

    @Test
    void 회원가입_성공() throws Exception {
      // setup
      var request = new Join("diger", "digerpassword1!", "diger@suwon.ac.kr");

      // execution
      var result = post(Uri.of(endpoint), request);

      // result validation
      validate(result, status().isOk(), Pair.of("$.success", true));

      // db validation
      Optional<User> saved = userRepository.findByLoginId("diger");
      assertAll(
        () -> assertThat(saved).isNotEmpty(),
        () -> assertThat(saved.get().getEmail()).isEqualTo(request.email()),
        () -> assertTrue(passwordEncoder.matches(request.password(), saved.get().getPassword()))
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("회원가입 API")
          .description("""
            회원가입 API입니다.
            Body에는 String 타입의 \"LoginId\", \"Password\", \"Email\"을 입력해야하며 모든 필드가 Blank 제약조건이 있습니다.
            """)
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @ParameterizedTest
    @MethodSource("duplicatedLoginIdAndEmail")
    void 회원가입_실패_아이디_이메일_중복(String loginId, String email) throws Exception {
      // setup
      var requestBody = new Join(loginId, "digerPassword123!", email);

      // execution
      var result = post(Uri.of(endpoint), requestBody);

      // result validation
      validate(result, status().isBadRequest(), LOGIN_ID_OR_EMAIL_OVERLAP);

      // docs
      result.andDo(
        RestDocument.builder()
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    static Stream<Arguments> duplicatedLoginIdAndEmail() {
      return Stream.of(
        Arguments.of(loginId, "test@suwiki.kr"),
        Arguments.of("test", email)
      );
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 회원가입_실패_잘못된_파라미터(String password) throws Exception {
      // setup
      var requestBody = new Join("diger", password, "test@suwiki.kr");

      // execution
      var result = post(Uri.of(endpoint), requestBody);

      // result validation
      validate(result, status().isBadRequest(), PARAMETER_VALIDATION_FAIL);

      // docs
      result.andDo(
        RestDocument.builder()
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 회원가입_실패_교내_이메일이_아님() throws Exception {
      // setup
      var requestBody = new Join("diger", "digerPassword123!", "diger@gmail.com");

      // execution
      var result = post(Uri.of(endpoint), requestBody);

      // result validation
      validate(result, status().isBadRequest(), INVALID_EMAIL_FORMAT);

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
  class 이메일_인증_테스트 {
    private final String endpoint = "/user/verify-email";

    @Test
    void 이메일_인증_성공() throws Exception {
      // setup
      var token = fixtures.가입_인증_토큰_생성(user.getId()).getToken();

      // execution
      var result = getHtml(Uri.of(endpoint), parameter("token", token));

      // result validation
      validateHtml(result, status().isOk(), SUCCESS.getContent());

      // db validation
      Optional<ConfirmationToken> confirmationToken = confirmationTokenRepository.findByToken(token);

      assertAll(
        () -> assertThat(confirmationToken).isNotEmpty(),
        () -> assertTrue(confirmationToken.get().isVerified())
      );

      result.andDo(document("test"));

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("이메일 인증 API")
          .description("""
            이메일 인증 API입니다.
            Parameter에는 'token'을 Key로 갖고 값을 입력해야합니다.
            """)
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 이메일_인증_실패_잘못된_토큰() throws Exception {
      // setup
      var token = fixtures.가입_인증_토큰_생성(user.getId()).getToken();

      // execution
      var result = getHtml(Uri.of(endpoint), parameter("token", token + "diger"));

      // result validation
      validateHtml(result, status().isOk(), ERROR.getContent());

      // docs
      result.andDo(
        RestDocument.builder()
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 이메일_인증_실패_만료된_토큰() throws Exception {
      // setup
      var token = fixtures.가입_인증_토큰_생성(user.getId()).getToken();
      var sut = Mockito.mock(ConfirmationToken.class);

      given(confirmationTokenRepository.findByToken(anyString())).willReturn(Optional.of(sut));
      given(sut.isExpired()).willReturn(true);

      // execution
      var result = getHtml(Uri.of(endpoint), parameter("token", token));

      // result validation
      validateHtml(result, status().isOk(), EXPIRED.getContent());

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
  class 아이디_찾기_테스트 {
    private final String endpoint = "/user/find-id";

    @Test
    void 아이디_찾기_성공() throws Exception {
      // setup
      var request = new FindId(email);

      // execution
      var result = post(Uri.of(endpoint), request);

      // result validation
      validate(result, status().isOk(), Pair.of("$.success", true));

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("아이디 찾기 API")
          .description("아이디 찾기 API입니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 아이디_찾기_실페_잘못된_파라미터(String email) throws Exception {
      // setup
      var request = new FindId(email);

      // execution
      var result = post(Uri.of(endpoint), request);

      // result validation
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
  class 비밀번호_찾기_테스트 {
    private final String endpoint = "/user/find-pw";

    @Test
    void 비밀번호_찾기_성공() throws Exception {
      // setup
      var request = new FindPassword(loginId, email);

      // execution
      var result = post(Uri.of(endpoint), request);

      // result validation
      validate(result, status().isOk(), Pair.of("$.success", true));

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("비밀번호 찾기 API")
          .description("비밀번호 찾기 API입니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 비밀번호_찾기_실패_이메일_아이디_불일치로_유저_조회_불가() throws Exception {
      // setup
      var request = new FindPassword(loginId, "wrongEaml@suwon.ac.kr");

      // execution
      var result = post(Uri.of(endpoint), request);

      // result validation
      validate(result, status().isNotFound(), USER_NOT_FOUND);

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
    void 비밀번호_찾기_실패_유효하지_않은_아이디(String loginId) throws Exception {
      // setup
      var requestBody = new FindPassword(loginId, email);

      // execution
      var result = post(Uri.of(endpoint), requestBody);

      // result validation
      validate(result, status().isBadRequest(), PARAMETER_VALIDATION_FAIL);

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
    void 비밀번호_찾기_실패_유효하지_않은_이메일(String email) throws Exception {
      // setup
      var requestBody = new FindPassword(loginId, email);

      // execution
      var result = post(Uri.of(endpoint), requestBody);

      // result validation
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
  class 비밀번호_변경_테스트 {
    private final String endpoint = "/user/reset-pw";

    @Test
    void 비밀번호_변경_성공() throws Exception {
      // setup
      String newPassword = "newPassword1!";
      var request = new EditPassword(password, newPassword);

      // execution
      var result = post(Uri.of(endpoint), accessToken, request);

      // result validation
      validate(result, status().isOk(), Pair.of("$.success", true));

      // db validation
      Optional<User> diger = userRepository.findByLoginId(loginId);
      assertAll(
        () -> assertThat(diger).isNotEmpty(),
        () -> assertTrue(passwordEncoder.matches(newPassword, diger.get().getPassword()))
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("비밀번호 변경 API")
          .description("비밀번호 변경 API입니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 비밀번호_변경_실패_비밀번호_틀림() throws Exception {
      // given
      var request = new EditPassword("wrongPassword", password);

      // when
      var result = post(Uri.of(endpoint), accessToken, request);

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
    void 비밀번호_변경_실패_기존과_같은_비밀번호() throws Exception {
      // given
      var request = new EditPassword(password, password);

      // when
      var result = post(Uri.of(endpoint), accessToken, request);

      // then
      validate(result, status().isBadRequest(), SAME_PASSWORD_WITH_OLD);

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
  class 로그인_테스트 {
    private final String mobileEndpoint = "/user/login";
    private final String webEndpoint = "/user/client-login";

    @Test
    void 웹_로그인_성공() throws Exception {
      // given
      fixtures.가입_인증된_토큰_생성(user.getId());
      var request = new UserRequest.Login(loginId, password);

      // when
      var result = post(Uri.of(webEndpoint), request);

      // then
      result.andExpect(status().isOk());

      var cookies = result.andReturn().getResponse().getCookies();
      assertThat(cookies).isNotNull()
        .allMatch(cookie -> cookie.getMaxAge() != 0);

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("웹 로그인 API")
          .description("로그인 API 웹 버전입니다. 토큰과 쿠키를 반환합니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 웹_로그인_실패_이메일_미인증_유저() throws Exception {
      // given
      var request = new UserRequest.Login(loginId, password);

      // when
      var result = post(Uri.of(webEndpoint), request);

      // then
      validate(result, status().isUnauthorized(), EMAIL_NOT_AUTHED);

      // docs
      result.andDo(
        RestDocument.builder()
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 웹_로그인_실패_잘못된_비밀번호() throws Exception {
      // given
      fixtures.가입_인증된_토큰_생성(user.getId());
      var request = new UserRequest.Login(loginId, "wrongPassword");

      // when
      var result = post(Uri.of(webEndpoint), request);

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
    void 모바일_로그인_성공() throws Exception {
      // given
      fixtures.가입_인증된_토큰_생성(user.getId());
      var request = new UserRequest.Login(loginId, password);

      // when
      var result = post(Uri.of(mobileEndpoint), request);

      // then
      result.andExpect(status().isOk());

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("모바일 로그인 API")
          .description("로그인 API 모바일 버전입니다. 토큰들을 반환합니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 모바일_로그인_실패_이메일_미인증_유저() throws Exception {
      // given
      var request = new UserRequest.Login(loginId, "wrongPassword");

      // when
      var result = post(Uri.of(mobileEndpoint), request);

      // then
      validate(result, status().isUnauthorized(), EMAIL_NOT_AUTHED);

      // docs
      result.andDo(
        RestDocument.builder()
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 모바일_로그인_실패_잘못된_비밀번호() throws Exception {
      // given
      fixtures.가입_인증된_토큰_생성(user.getId());
      var request = new UserRequest.Login(loginId, "wrongPassword");

      // when
      var result = post(Uri.of(mobileEndpoint), request);

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
  }

  @Nested
  class 로그아웃_테스트 {

    @Test
    void 로그아웃_성공() throws Exception {
      // given
      var payload = fixtures.리프레시_토큰_생성(user.getId());

      // when
      var result = post(Uri.of("/user/client-logout"), new Cookie("refreshToken", payload));

      // then
      result.andExpect(status().isOk());

      var cookies = result.andReturn().getResponse().getCookies();
      assertThat(cookies).isNotNull()
        .allMatch(cookie -> cookie.getMaxAge() == 0);

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("웹 로그아웃 API")
          .description("로그아웃 API 웹 버전입니다. 쿠키의 시간을 만료시킵니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Nested
  class 마이페이지_테스트 {
    private final String endpoint = "/user/my-page";

    @Test
    void 마이페이지_조회_성공() throws Exception {
      // given

      // when
      var result = get(Uri.of(endpoint), accessToken);

      // then
      result.andExpectAll(
        status().isOk(),
        jsonPath("loginId").value(user.getLoginId()),
        jsonPath("email").value(user.getEmail()),
        jsonPath("point").value(user.getPoint()),
        jsonPath("writtenEvaluation").value(user.getWrittenEvaluation()),
        jsonPath("writtenExam").value(user.getWrittenExam()),
        jsonPath("viewExam").value(user.getViewExamCount())
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("[토큰 필요] 마이페이지 조회 API")
          .description("내 정보를 조회하는 API입니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Nested
  class 토큰_재발급_테스트 {
    private final String webEndpoint = "/user/client-refresh";
    private final String mobileEndpoint = "/user/refresh";

    @Test
    void 웹_재발급_성공() throws Exception {
      // given
      var refreshToken = fixtures.리프레시_토큰_생성(user.getId());

      // when
      var result = post(Uri.of(webEndpoint), new Cookie("refreshToken", refreshToken));

      // then
      result.andExpect(status().isOk());

      var cookies = result.andReturn().getResponse().getCookies();
      assertThat(cookies).isNotNull()
        .allMatch(cookie -> cookie.getMaxAge() != 0);

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("[쿠키 필요] 웹 토큰 재발급 API")
          .description("토큰 재발급 API 웹 버전입니다. 쿠키에 저장된 리프레시 토큰의 시간을 갱신합니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 웹_토큰_재발급_실패_유효하지_않은_토큰() throws Exception {
      // given

      // when
      var result = post(Uri.of(webEndpoint), new Cookie("refreshToken", INVALID_ACCESS_TOKEN));

      // then
      validate(result, status().isBadRequest(), INVALID_TOKEN);

      // docs
      result.andDo(
        RestDocument.builder()
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 모바일_재발급_성공() throws Exception {
      // given
      var refreshToken = fixtures.리프레시_토큰_생성(user.getId());

      // when
      var result = post(Uri.of(mobileEndpoint), refreshToken);

      // then
      result.andExpectAll(
        status().isOk(),
        jsonPath("$.AccessToken").exists(),
        jsonPath("$.RefreshToken").exists()
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("[쿠키 필요] 웹 토큰 재발급 API")
          .description("토큰 재발급 API 웹 버전입니다. 쿠키에 저장된 리프레시 토큰의 시간을 갱신합니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 모바일_토큰_재발급_실패_유효하지_않은_토큰() throws Exception {
      // given

      // when
      var result = post(Uri.of(mobileEndpoint), INVALID_ACCESS_TOKEN);

      // then
      validate(result, status().isBadRequest(), INVALID_TOKEN);

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
  class 전공_즐겨찾기_테스트 {
    private final String endpoint = "/user/favorite-major";

    @Test
    void 전공_즐겨찾기_등록_성공() throws Exception {
      // given
      var request = new MajorRequest(Major.values()[0].name());

      // when
      var result = post(Uri.of(endpoint), accessToken, request);

      // then
      result.andExpect(status().isOk());

      var majors = favoriteMajorRepository.findAllByUser(user.getId());
      assertThat(majors).isNotEmpty().hasSize(1);

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("[토큰 필요] 전공 즐겨찾기 등록 API")
          .description("전공 즐겨찾기 등록 API입니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 전공_즐겨찾기_등록_실패_잘못된_파라미터(String major) throws Exception {
      // given
      var request = new MajorRequest(major);

      // when
      var result = post(Uri.of(endpoint), accessToken, request);

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

    @Transactional
    @Test
    void 전공_즐겨찾기_삭제_성공() throws Exception {
      // given
      var major = fixtures.전공_즐겨찾기_생성(user.getId());

      // when
      var result = delete(Uri.of(endpoint), accessToken, null, parameter("majorType", major));

      // then
      result.andExpect(status().isOk());

      var majors = favoriteMajorRepository.findAllByUser(user.getId());
      assertThat(majors).isEmpty();

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("[토큰 필요] 전공 즐겨찾기 삭제 API")
          .description("즐겨찾기로 등록한 전공을 삭제하는 API입니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 전공_즐겨찾기_삭제_실패_잘못된_파라미터() throws Exception {
      // given

      // when
      var result = delete(Uri.of(endpoint), accessToken, null, parameter("majorType", null));

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

    @Transactional
    @Test
    void 전공_즐겨찾기_조회_성공() throws Exception {
      // given
      var majors = fixtures.전공_즐겨찾기_여러개_생성(user.getId());

      // when
      var result = get(Uri.of(endpoint), accessToken);

      // then
      result.andExpectAll(
        status().isOk(),
        jsonPath("$.data").exists(),
        jsonPath("$.data.size()").value(majors.size()),
        jsonPath("$.data[0]").value(majors.get(0))
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("[토큰 필요] 전공 즐겨찾기 조회 API")
          .description("즐겨찾기한 모든 전공을 조회하는 API 입니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Nested
  class 제한_내역_조회_테스트 {

    @Test
    void 이용제한_내역_조회_성공() throws Exception {
      // given
      var restrictingUser = fixtures.이용제한_내역_생성(user.getId());

      // when
      var result = get(Uri.of("/user/restricted-reason"), accessToken);

      // then
      result.andExpectAll(
        status().isOk(),
        jsonPath("$.[0]").exists(),
        jsonPath("$.[0].restrictedReason").value(restrictingUser.getRestrictingReason()),
        jsonPath("$.[0].judgement").value(restrictingUser.getJudgement())
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("[토큰 필요] 이용제한 내역 조회 API")
          .description("이용제한된 내역을 조회하는 API 입니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 블랙리스트_내역_조회_성공() throws Exception {
      // given
      var blacklistDomain = fixtures.블랙_리스트_생성(user.getId());

      // when
      var result = get(Uri.of("/user/blacklist-reason"), accessToken);

      // then
      result.andExpectAll(
        status().isOk(),
        jsonPath("$.[0]").exists(),
        jsonPath("$.[0].blackListReason").value(blacklistDomain.getBannedReason()),
        jsonPath("$.[0].judgement").value(blacklistDomain.getJudgement())
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("[토큰 필요] 블랙리스트 내역 조회 API")
          .description("영구 정지 사유를 조회하는 API 입니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Nested
  class 회원탈퇴_테스트 {

    private final String endpoint = "/user/quit";

    @Transactional
    @Test
    void 회원탈퇴_성공() throws Exception {
      // given
      var request = new UserRequest.Quit(loginId, password);

      var lecture = fixtures.강의_생성();

      fixtures.전공_즐겨찾기_생성(user.getId());
      fixtures.강의평가_생성(user.getId(), lecture);
      fixtures.시험평가_생성(user.getId(), lecture);

      // when
      var result = delete(Uri.of(endpoint), accessToken, request);

      // then
      result.andExpect(status().isOk());

      var saved = userRepository.findById(user.getId()).orElseThrow();
      var majors = favoriteMajorRepository.findAllByUser(user.getId());

      assertAll(
        () -> assertThat(majors).isEmpty(),
        () -> assertThat(saved).isNotNull(),
        () -> assertThat(saved.getRole()).isNull(),
        () -> assertThat(saved.isRestricted()).isTrue()
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .summary("[토큰 필요] 회원 탈퇴 API")
          .description("회원 탈퇴 API입니다. 작성한 게시글, 신고 내역, 구매한 시험 정보 등 요청한 유저의 이력은 모두 제거됩니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 회원탈퇴_실패_비밀번호_불일치() throws Exception {
      // given
      var request = new UserRequest.Quit(loginId, "wrongPassword");

      // when
      var result = delete(Uri.of(endpoint), accessToken, request);

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
  }
}

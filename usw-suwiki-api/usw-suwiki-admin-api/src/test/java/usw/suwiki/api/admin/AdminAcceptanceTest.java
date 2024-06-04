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
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.UserRepository;
import usw.suwiki.domain.user.dto.UserRequest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
        jsonPath("$.AccessToken").exists(),
        jsonPath("$.UserCount").exists()
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
}

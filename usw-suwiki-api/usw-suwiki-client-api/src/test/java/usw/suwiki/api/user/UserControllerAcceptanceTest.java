package usw.suwiki.api.user;

import io.github.hejow.restdocs.document.RestDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.util.Pair;
import usw.suwiki.auth.token.ConfirmationToken;
import usw.suwiki.auth.token.ConfirmationTokenRepository;
import usw.suwiki.common.test.annotation.AcceptanceTest;
import usw.suwiki.common.test.support.AcceptanceTestSupport;
import usw.suwiki.common.test.support.Uri;
import usw.suwiki.core.secure.PasswordEncoder;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.UserRepository;
import usw.suwiki.domain.user.dto.UserRequestDto.CheckEmailForm;
import usw.suwiki.domain.user.dto.UserRequestDto.CheckLoginIdForm;
import usw.suwiki.domain.user.dto.UserRequestDto.EditMyPasswordForm;
import usw.suwiki.domain.user.dto.UserRequestDto.FindIdForm;
import usw.suwiki.domain.user.dto.UserRequestDto.FindPasswordForm;
import usw.suwiki.domain.user.dto.UserRequestDto.JoinForm;
import usw.suwiki.test.fixture.Fixtures;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static usw.suwiki.auth.token.response.ConfirmResponse.ERROR;
import static usw.suwiki.auth.token.response.ConfirmResponse.EXPIRED;
import static usw.suwiki.auth.token.response.ConfirmResponse.SUCCESS;
import static usw.suwiki.common.test.Tag.USER;
import static usw.suwiki.common.test.support.Pair.parameter;
import static usw.suwiki.common.test.support.ResponseValidator.validate;
import static usw.suwiki.common.test.support.ResponseValidator.validateHtml;
import static usw.suwiki.core.exception.ExceptionType.IS_NOT_EMAIL_FORM;
import static usw.suwiki.core.exception.ExceptionType.LOGIN_ID_OR_EMAIL_OVERLAP;
import static usw.suwiki.core.exception.ExceptionType.PARAMETER_VALIDATION_FAIL;

@ExtendWith(MockitoExtension.class)
@AcceptanceTest
class UserControllerAcceptanceTest extends AcceptanceTestSupport {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private Fixtures fixtures;

  @SpyBean
  private ConfirmationTokenRepository confirmationTokenRepository;

  private User user;
  private String accessToken;
  private ConfirmationToken confirmationToken;

  private final String loginId = "suwiki";
  private final String password = "p@sSw0rc1!!";
  private final String email = "suwiki@suwon.ac.kr";

  @BeforeEach
  public void setup() {
    user = userRepository.save(User.init(loginId, passwordEncoder.encode(password), email).activate());
    confirmationToken = fixtures.가입_인증_토큰_생성(user.getId());
    accessToken = fixtures.토큰_생성(user);
  }

  @Nested
  class 유저_아이디_중복_확인_테스트 {
    private final String endpoint = "/user/check-id";

    @Test
    void 아이디_중복확인_성공_중복일_시() throws Exception {
      // setup
      var request = new CheckLoginIdForm(loginId);

      // execution
      var result = post(Uri.of(endpoint), request);

      // result validation
      validate(result, status().isOk(), Pair.of("$.overlap", true));

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("check-id")
          .summary("아이디 중복 확인 (중복일 시) API")
          .description("아이디 중복 확인 (중복일 시) API입니다. Body에는 String 타입을 입력해야하며 Blank 제약조건이 있습니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 아이디_중복확인_성공_중복_아닐_시() throws Exception {
      // setup
      var request = new CheckLoginIdForm("diger");

      // execution
      var result = post(Uri.of(endpoint), request);

      // result validation
      validate(result, status().isOk(), Pair.of("$.overlap", false));

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("check-id")
          .summary("아이디 중복 확인 (중복 아닐 시) API")
          .description("아이디 중복 확인 (중복 아닐 시) API입니다. Body에는 String 타입을 입력해야하며 Blank 제약조건이 있습니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 아이디_중복확인_실패_잘못된_요청값() throws Exception {
      // setup
      var request = new CheckLoginIdForm("");

      // execution
      var result = post(Uri.of(endpoint), request);

      // result validation
      validate(result, status().isBadRequest(), PARAMETER_VALIDATION_FAIL);
    }
  }

  @Nested
  class 유저_이메일_중복_확인_테스트 {
    private final String endpoint = "/user/check-email";

    @Test
    void 아이디_중복확인_성공_중복일_시() throws Exception {
      // setup
      var request = new CheckEmailForm(email);

      // execution
      var result = post(Uri.of(endpoint), request);

      // result validation
      validate(result, status().isOk(), Pair.of("$.overlap", true));

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("check-email")
          .summary("이메일 중복 확인 (중복일 시) API")
          .description("이메일 중복 확인 (중복일 시) API입니다. Body에는 String 타입을 입력해야하며 Blank 제약조건이 있습니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 아이디_중복확인_성공_중복_아닐_시() throws Exception {
      // setup
      var request = new CheckEmailForm("diger@suwon.ac.kr");

      // execution
      var result = post(Uri.of(endpoint), request);

      // result validation
      validate(result, status().isOk(), Pair.of("$.overlap", false));

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("check-email")
          .summary("이메일 중복 확인 (중복일 시) API")
          .description("이메일 중복 확인 (중복일 시) API입니다. Body에는 String 타입을 입력해야하며 Blank 제약조건이 있습니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }

    @Test
    void 아이디_중복확인_실패_잘못된_요청값() throws Exception {
      // setup
      var request = new CheckEmailForm("");

      // execution
      var result = post(Uri.of(endpoint), request);

      // result validation
      validate(result, status().isBadRequest(), PARAMETER_VALIDATION_FAIL);
    }
  }

  @Nested
  class 유저_회원가입_테스트 {
    private final String endpoint = "/user/join";

    @Test
    void 회원가입_성공() throws Exception {
      // setup
      var request = new JoinForm("diger", "digerpassword1!", "diger@suwon.ac.kr");

      // execution
      var result = post(Uri.of(endpoint), request);

      // result validation
      validate(result, status().isOk(), Pair.of("$.success", true));

      // db validation
      Optional<User> diger = userRepository.findByLoginId("diger");
      assertAll(
        () -> assertThat(diger).isNotEmpty(),
        () -> assertThat(diger.get().getEmail()).isEqualTo(request.email()),
        () -> assertTrue(passwordEncoder.matches(request.password(), diger.get().getPassword()))
      );

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("user-join")
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

    @Test
    void 회원가입_실패_아이디_중복() throws Exception {
      // setup
      var requestBody = new JoinForm(loginId, "digerPassword123!", "diger@gmail.com");

      // execution
      var result = post(Uri.of(endpoint), requestBody);

      // result validation
      validate(result, status().isBadRequest(), LOGIN_ID_OR_EMAIL_OVERLAP);
    }

    @Test
    void 회원가입_실패_이메일_중복() throws Exception {
      // setup
      var requestBody = new JoinForm("diger", "digerPassword123!", email);

      // execution
      var result = post(Uri.of(endpoint), requestBody);

      // result validation
      validate(result, status().isBadRequest(), LOGIN_ID_OR_EMAIL_OVERLAP);
    }

    @Test
    void 회원가입_실패_값_누락() throws Exception {
      // setup
      var requestBody = new JoinForm("diger", "", "test@suwiki.kr");

      // execution
      var result = post(Uri.of(endpoint), requestBody);

      // result validation
      validate(result, status().isBadRequest(), PARAMETER_VALIDATION_FAIL);
    }

    @Test
    void 회원가입_실패_교내_이메일이_아님() throws Exception {
      // setup
      var requestBody = new JoinForm("diger", "digerPassword123!", "diger@gmail.com");

      // execution
      var result = post(Uri.of(endpoint), requestBody);

      // result validation
      validate(result, status().isBadRequest(), IS_NOT_EMAIL_FORM);
    }
  }

  @Nested
  class 유저_이메일_인증_테스트 {
    private final String endpoint = "/user/verify-email";

    @Test
    void 이메일_인증_성공() throws Exception {
      // setup
      final String emailVerificationToken = confirmationToken.getToken();

      // execution
      var result = getHtml(Uri.of(endpoint), parameter("token", emailVerificationToken));

      // result validation
      validateHtml(result, status().isOk(), SUCCESS.getContent());

      // db validation
      Optional<ConfirmationToken> confirmationToken = confirmationTokenRepository.findByToken(emailVerificationToken);

      assertAll(
        () -> assertThat(confirmationToken).isNotEmpty(),
        () -> assertTrue(confirmationToken.get().isVerified())
      );

      // json parse error
//      result.andDo(
//        RestDocument.builder()
//          .identifier("verify-email")
//          .summary("이메일 인증 API")
//          .description("이메일 인증 API입니다. Parameter에는 \"token\"을 Key로 갖고 값을 입력해야합니다.")
//          .tag(USER)
//          .result(result)
//          .generateDocs()
//      );
    }

    @Test
    void 이메일_인증_실패_잘못된_토큰() throws Exception {
      // setup
      final String emailVerificationToken = confirmationToken.getToken();

      // execution
      var result = getHtml(Uri.of(endpoint), parameter("token", emailVerificationToken + "diger"));

      // result validation
      validateHtml(result, status().isOk(), ERROR.getContent());
    }

    @Test
    void 이메일_인증_실패_만료된_토큰() throws Exception {
      // setup
      var sut = Mockito.mock(ConfirmationToken.class);

      given(confirmationTokenRepository.findByToken(anyString())).willReturn(Optional.of(sut));
      given(sut.isExpired()).willReturn(true);

      // execution
      var result = getHtml(Uri.of(endpoint), parameter("token", confirmationToken.getToken()));

      // result validation
      validateHtml(result, status().isOk(), EXPIRED.getContent());
    }
  }

  @Nested
  class 아이디_찾기_테스트 {
    private final String endpoint = "/user/find-id";

    @Test
    void 아이디_찾기_성공() throws Exception {
      // setup
      var requestBody = new FindIdForm(email);

      // execution
      var result = post(Uri.of(endpoint), requestBody);

      // result validation
      validate(result, status().isOk(), Pair.of("$.success", true));

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("find-id")
          .summary("아이디 찾기 API")
          .description("아이디 찾기 API입니다.")
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
      var requestBody = new FindPasswordForm(loginId, email);

      // execution
      var result = post(Uri.of(endpoint), requestBody);

      // result validation
      validate(result, status().isOk(), Pair.of("$.success", true));

      // docs
      result.andDo(
        RestDocument.builder()
          .identifier("find-id")
          .summary("비밀번호 찾기 API")
          .description("비밀번호 찾기 API입니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }
  }

  @Nested
  class 비밀번호_초기화_테스트 {
    private final String endpoint = "/user/reset-pw";

    @Test
    void 비밀번호_초기화_성공() throws Exception {
      // setup
      String newPassword = "newPassword1!";
      var request = new EditMyPasswordForm(password, newPassword);

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
          .identifier("reset-id")
          .summary("비밀번호 초기화 API")
          .description("비밀번호 초기화 API입니다.")
          .tag(USER)
          .result(result)
          .generateDocs()
      );
    }
  }
}

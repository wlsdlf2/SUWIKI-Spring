package usw.suwiki.api.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import usw.suwiki.auth.core.annotation.Authenticated;
import usw.suwiki.auth.core.annotation.Authorize;
import usw.suwiki.common.response.ResponseForm;
import usw.suwiki.core.secure.model.Tokens;
import usw.suwiki.domain.user.dto.MajorRequest;
import usw.suwiki.domain.user.dto.UserRequest;
import usw.suwiki.domain.user.dto.UserResponse;
import usw.suwiki.domain.user.service.AuthService;
import usw.suwiki.domain.user.service.ConfirmationTokenService;
import usw.suwiki.domain.user.service.UserService;
import usw.suwiki.statistics.annotation.Statistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;
import static usw.suwiki.domain.user.dto.UserResponse.BlackedReason;
import static usw.suwiki.domain.user.dto.UserResponse.RestrictedReason;
import static usw.suwiki.statistics.log.MonitorTarget.USER;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;
  private final AuthService authService;
  private final ConfirmationTokenService confirmationTokenService;

  @Statistics(USER)
  @PostMapping("/check-id")
  @ResponseStatus(OK)
  public UserResponse.Overlap isOverlapId(@Valid @RequestBody UserRequest.CheckLoginId request) {
    var isDuplicated = authService.isDuplicatedId(request.loginId());
    return new UserResponse.Overlap(isDuplicated);
  }

  @Statistics(USER)
  @PostMapping("/check-email")
  @ResponseStatus(OK)
  public UserResponse.Overlap isOverlapEmail(@Valid @RequestBody UserRequest.CheckEmail request) {
    var isDuplicated = authService.isDuplicatedEmail(request.email());
    return new UserResponse.Overlap(isDuplicated);
  }

  @Statistics(USER)
  @PostMapping("join")
  @ResponseStatus(OK)
  public UserResponse.Success join(@Valid @RequestBody UserRequest.Join request) {
    authService.join(request.loginId(), request.password(), request.email());
    return new UserResponse.Success(true);
  }

  // todo: confirmationControllerV2와 같은 코드
  @Statistics(USER)
  @GetMapping(value = "verify-email", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
  @ResponseStatus(OK)
  public String confirmEmail(@RequestParam("token") String token) {
    return confirmationTokenService.confirm(token);
  }

  @Statistics(USER)
  @PostMapping("find-id")
  @ResponseStatus(OK)
  public UserResponse.Success findId(@Valid @RequestBody UserRequest.FindId request) {
    authService.findId(request.email());
    return new UserResponse.Success(true);
  }

  @Statistics(USER)
  @PostMapping("find-pw")
  @ResponseStatus(OK)
  public UserResponse.Success findPw(@Valid @RequestBody UserRequest.FindPassword request) {
    authService.findPw(request.loginId(), request.email());
    return new UserResponse.Success(true);
  }

  @Authorize
  @Statistics(USER)
  @PostMapping("reset-pw") // TODO (05.31) 이름은 초기화 API 인데 그냥 비밀번호 변경 API
  @ResponseStatus(OK)
  public UserResponse.Success editPassword(@Authenticated Long id, @Valid @RequestBody UserRequest.EditPassword request) {
    userService.changePassword(id, request.prePassword(), request.newPassword());
    return new UserResponse.Success(true);
  }

  @Statistics(USER)
  @PostMapping("/login")
  @ResponseStatus(OK)
  public Tokens mobileLogin(@Valid @RequestBody UserRequest.Login request) {
    return authService.login(request.loginId(), request.password());
  }

  @Statistics(USER)
  @PostMapping("/client-login")
  @ResponseStatus(OK)
  public Map<String, String> webLogin(@Valid @RequestBody UserRequest.Login request, HttpServletResponse response) {
    var tokens = authService.login(request.loginId(), request.password());

    var refreshCookie = new Cookie("refreshToken", tokens.getRefreshToken());
    refreshCookie.setMaxAge(270 * 24 * 60 * 60);
    refreshCookie.setSecure(true);
    refreshCookie.setHttpOnly(true);
    response.addCookie(refreshCookie);

    return new HashMap<>() {{
      put("AccessToken", tokens.getAccessToken());
    }};
  }

  @Statistics(USER)
  @PostMapping("/client-logout")
  @ResponseStatus(OK)
  public Map<String, Boolean> logout(HttpServletResponse response) {
    var cookie = new Cookie("refreshToken", "");
    cookie.setMaxAge(0);
    response.addCookie(cookie);

    return new HashMap<>() {{
      put("Success", true);
    }};
  }

  @Authorize
  @Statistics(USER)
  @GetMapping("/my-page")
  @ResponseStatus(OK)
  public UserResponse.MyPage myPage(@Authenticated Long userId) {
    return userService.loadMyPage(userId);
  }

  @Statistics(USER)
  @PostMapping("/client-refresh")
  @ResponseStatus(OK)
  public Map<String, String> reissueWeb(@CookieValue(value = "refreshToken") Cookie cookie, HttpServletResponse response) {
    var token = authService.reissue(cookie.getValue());

    var newCookie = new Cookie("refreshToken", token.getRefreshToken());
    newCookie.setMaxAge(14 * 24 * 60 * 60);
    newCookie.setSecure(true);
    newCookie.setHttpOnly(true);
    response.addCookie(newCookie);

    return new HashMap<>() {{
      put("AccessToken", token.getAccessToken());
    }};
  }

  @Statistics(USER)
  @PostMapping("/refresh")
  @ResponseStatus(OK)
  public Tokens reissueMobile(@Valid @RequestHeader String Authorization) { // refresh 토큰임
    return authService.reissue(Authorization); // todo: (05.31) change to id
  }

  @Authorize
  @Statistics(USER)
  @PostMapping("/favorite-major")
  @ResponseStatus(OK)
  public void saveFavoriteMajor(@Authenticated Long userId, @Valid @RequestBody MajorRequest request) {
    userService.saveFavoriteMajor(userId, request.getMajorType());
  }

  @Authorize
  @Statistics(USER)
  @DeleteMapping("/favorite-major")
  @ResponseStatus(OK)
  public void deleteFavoriteMajor(@Authenticated Long userId, @RequestParam String majorType) {
    userService.deleteFavoriteMajor(userId, majorType);
  }

  @Authorize
  @Statistics(USER)
  @GetMapping("/favorite-major")
  @ResponseStatus(OK)
  public ResponseForm loadFavoriteMajors(@Authenticated Long userId) {
    var response = userService.loadAllFavoriteMajors(userId);
    return new ResponseForm(response);
  }

  @Statistics(USER)
  @GetMapping(value = "/suki", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
  @ResponseStatus(OK)
  public String thanksToSuwiki() {
    return """
        <center>\uD83D\uDE00 Thank You Suki! \uD83D\uDE00 <br><br> You gave to me a lot of knowledge <br><br>
          He is my Tech-Mentor <br><br>
          If you wanna contact him <br><br>
          <a href = https://github.com/0xsuky>
          <b>https://github.com/0xsuky<b>
        </center>
      """;
  }

  @Authorize
  @Statistics(USER)
  @GetMapping("/restricted-reason")
  @ResponseStatus(OK) // todo: RestrictingUserControllerV2 동일한 API
  public List<RestrictedReason> loadRestrictedReason(@Authenticated Long userId) {
    return userService.loadRestrictedReason(userId);
  }

  @Authorize
  @Statistics(USER)
  @GetMapping("/blacklist-reason")
  @ResponseStatus(OK) // todo: BlacklistDomainControllerV2 동일한 API
  public List<BlackedReason> loadBlacklistReason(@Authenticated Long userId) {
    return userService.loadBlackListReason(userId);
  }

  @Authorize
  @Statistics(USER)
  @DeleteMapping("/quit")
  @ResponseStatus(OK)
  public UserResponse.Success quit(@Authenticated Long userId, @Valid @RequestBody UserRequest.Quit request) {
    userService.quit(userId, request.password());
    return new UserResponse.Success(true);
  }
}

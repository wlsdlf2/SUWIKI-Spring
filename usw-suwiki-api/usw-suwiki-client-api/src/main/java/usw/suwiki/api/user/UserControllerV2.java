package usw.suwiki.api.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import usw.suwiki.auth.core.annotation.Authenticated;
import usw.suwiki.auth.core.annotation.Authorize;
import usw.suwiki.common.response.ResponseForm;
import usw.suwiki.domain.user.dto.UserRequest;
import usw.suwiki.domain.user.service.UserBusinessService;
import usw.suwiki.statistics.annotation.Statistics;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;
import static usw.suwiki.statistics.log.MonitorTarget.USER;

@RestController
@RequestMapping("/v2/user")
@RequiredArgsConstructor
public class UserControllerV2 {
  private final UserBusinessService userBusinessService;

  @Statistics(USER)
  @PostMapping("/loginId/check")
  @ResponseStatus(OK)
  public ResponseForm overlapId(@Valid @RequestBody UserRequest.CheckLoginId checkLoginId) {
    var response = userBusinessService.isDuplicatedId(checkLoginId.loginId());
    return ResponseForm.success(response);
  }

  @Statistics(USER)
  @PostMapping("/email/check")
  @ResponseStatus(OK)
  public ResponseForm overlapEmail(@Valid @RequestBody UserRequest.CheckEmail checkEmail) {
    var response = userBusinessService.isDuplicatedEmail(checkEmail.email());
    return ResponseForm.success(response);
  }

  @Statistics(USER)
  @PostMapping
  @ResponseStatus(OK)
  public ResponseForm join(@Valid @RequestBody UserRequest.Join join) {
    Map<?, ?> response = userBusinessService.join(join.loginId(), join.password(), join.email());
    return ResponseForm.success(response);
  }

  @Statistics(USER)
  @PostMapping("inquiry-loginId")
  @ResponseStatus(OK)
  public ResponseForm findId(@Valid @RequestBody UserRequest.FindId findId) {
    var response = userBusinessService.findId(findId.email());
    return ResponseForm.success(response);
  }

  @Statistics(USER)
  @PostMapping("inquiry-password")
  @ResponseStatus(OK)
  public ResponseForm findPw(@Valid @RequestBody UserRequest.FindPassword findPassword) {
    var response = userBusinessService.findPw(findPassword.loginId(), findPassword.email());
    return ResponseForm.success(response);
  }

  @Authorize
  @Statistics(USER)
  @PatchMapping("password")
  @ResponseStatus(OK)
  public ResponseForm resetPw(@Authenticated Long userId, @Valid @RequestBody UserRequest.EditPassword request) {
    var response = userBusinessService.editPassword(userId, request.prePassword(), request.newPassword());
    return ResponseForm.success(response);
  }

  @Statistics(USER)
  @PostMapping("mobile-login")
  @ResponseStatus(OK)
  public ResponseForm mobileLogin(@Valid @RequestBody UserRequest.Login request) {
    return ResponseForm.success(userBusinessService.login(request.loginId(), request.password()));
  }

  @Statistics(USER)
  @PostMapping("web-login")
  @ResponseStatus(OK)
  public ResponseForm webLogin(
    @Valid @RequestBody UserRequest.Login login,
    HttpServletResponse response
  ) {
    Map<String, String> tokenPair = userBusinessService.login(login.loginId(), login.password());

    Cookie refreshCookie = new Cookie("refreshToken", tokenPair.get("RefreshToken"));
    refreshCookie.setMaxAge(270 * 24 * 60 * 60);
    refreshCookie.setSecure(true);
    refreshCookie.setHttpOnly(true);
    response.addCookie(refreshCookie);

    return ResponseForm.success(new HashMap<>() {{
      put("AccessToken", tokenPair.get("AccessToken"));
    }});
  }

  @Authorize
  @Statistics(USER)
  @PostMapping("client-logout")
  @ResponseStatus(OK)
  public ResponseForm clientLogout(HttpServletResponse response) {
    Cookie refreshCookie = new Cookie("refreshToken", "");
    refreshCookie.setMaxAge(0);
    response.addCookie(refreshCookie);

    return ResponseForm.success(new HashMap<>() {{
      put("Success", true);
    }});
  }

  @Authorize
  @Statistics(USER)
  @GetMapping
  @ResponseStatus(OK)
  public ResponseForm myPage(@Authenticated Long userId) {
    var response = userBusinessService.loadMyPage(userId);
    return ResponseForm.success(response);
  }

  @Authorize
  @Statistics(USER)
  @DeleteMapping
  @ResponseStatus(OK)
  public ResponseForm quit(@Authenticated Long userId, @Valid @RequestBody UserRequest.Quit quit) {
    return ResponseForm.success(userBusinessService.quit(userId, quit.password()));
  }
}

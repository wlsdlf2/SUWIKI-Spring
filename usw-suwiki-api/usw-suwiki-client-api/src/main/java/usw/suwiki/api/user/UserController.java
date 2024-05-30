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
import usw.suwiki.auth.token.service.ConfirmationTokenBusinessService;
import usw.suwiki.common.response.ResponseForm;
import usw.suwiki.domain.user.dto.MajorRequest;
import usw.suwiki.domain.user.dto.UserRequest;
import usw.suwiki.domain.user.service.UserBusinessService;
import usw.suwiki.statistics.annotation.Statistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;
import static usw.suwiki.domain.user.dto.UserResponse.LoadMyBlackListReasonResponse;
import static usw.suwiki.domain.user.dto.UserResponse.LoadMyRestrictedReasonResponse;
import static usw.suwiki.domain.user.dto.UserResponse.UserInformationResponse;
import static usw.suwiki.statistics.log.MonitorTarget.USER;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
  private final UserBusinessService userBusinessService;
  private final ConfirmationTokenBusinessService confirmationTokenBusinessService;

  @Statistics(USER)
  @PostMapping("/check-id")
  @ResponseStatus(OK)
  public Map<String, Boolean> overlapId(@Valid @RequestBody UserRequest.CheckLoginId request) {
    return userBusinessService.isDuplicatedId(request.loginId());
  }

  @Statistics(USER)
  @PostMapping("/check-email")
  @ResponseStatus(OK)
  public Map<String, Boolean> overlapEmail(@Valid @RequestBody UserRequest.CheckEmail request) {
    return userBusinessService.isDuplicatedEmail(request.email());
  }

  @Statistics(USER)
  @PostMapping("join")
  @ResponseStatus(OK)
  public Map<String, Boolean> join(@Valid @RequestBody UserRequest.Join request) {
    return userBusinessService.join(request.loginId(), request.password(), request.email());
  }

  // todo: confirmationControllerV2와 같은 코드
  @Statistics(USER)
  @GetMapping(value = "verify-email", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
  @ResponseStatus(OK)
  public String confirmEmail(@RequestParam("token") String token) {
    return confirmationTokenBusinessService.confirmToken(token);
  }

  @Statistics(USER)
  @PostMapping("find-id")
  @ResponseStatus(OK)
  public Map<String, Boolean> findId(@Valid @RequestBody UserRequest.FindId request) {
    return userBusinessService.findId(request.email());
  }

  @Statistics(USER)
  @PostMapping("find-pw")
  @ResponseStatus(OK)
  public Map<String, Boolean> findPw(@Valid @RequestBody UserRequest.FindPassword request) {
    return userBusinessService.findPw(request.loginId(), request.email());
  }

  @Authorize
  @Statistics(USER)
  @PostMapping("reset-pw") // TODO (05.31) 이름은 초기화 API 인데 그냥 비밀번호 변경 API
  @ResponseStatus(OK)
  public Map<String, Boolean> editPassword(@Authenticated Long id, @Valid @RequestBody UserRequest.EditPassword request) {
    return userBusinessService.editPassword(id, request.prePassword(), request.newPassword());
  }

  @Statistics(USER)
  @PostMapping("/login")
  @ResponseStatus(OK)
  public Map<String, String> mobileLogin(@Valid @RequestBody UserRequest.Login request) {
    return userBusinessService.login(request.loginId(), request.password());
  }

  @Statistics(USER)
  @PostMapping("/client-login")
  @ResponseStatus(OK)
  public Map<String, String> webLogin(@Valid @RequestBody UserRequest.Login request, HttpServletResponse response) {
    var tokens = userBusinessService.login(request.loginId(), request.password());

    var refreshCookie = new Cookie("refreshToken", tokens.get("RefreshToken"));
    refreshCookie.setMaxAge(270 * 24 * 60 * 60);
    refreshCookie.setSecure(true);
    refreshCookie.setHttpOnly(true);
    response.addCookie(refreshCookie);

    return new HashMap<>() {{
      put("AccessToken", tokens.get("AccessToken"));
    }};
  }

  @Statistics(USER)
  @PostMapping("/client-logout")
  @ResponseStatus(OK)
  public Map<String, Boolean> webLogout(HttpServletResponse response) {
    Cookie refreshCookie = new Cookie("refreshToken", "");
    refreshCookie.setMaxAge(0);
    response.addCookie(refreshCookie);

    return new HashMap<>() {{
      put("Success", true);
    }};
  }

  @Authorize
  @Statistics(USER)
  @GetMapping("/my-page")
  @ResponseStatus(OK)
  public UserInformationResponse myPage(@Authenticated Long userId) {
    return userBusinessService.loadMyPage(userId);
  }

  @Statistics(USER)
  @PostMapping("/client-refresh")
  @ResponseStatus(OK)
  public Map<String, String> reissueWeb(@CookieValue(value = "refreshToken") Cookie cookie, HttpServletResponse response) {
    Map<String, String> tokens = userBusinessService.reissueWeb(cookie);

    var newCookie = new Cookie("refreshToken", tokens.get("RefreshToken"));
    newCookie.setMaxAge(14 * 24 * 60 * 60);
    newCookie.setSecure(true);
    newCookie.setHttpOnly(true);
    response.addCookie(newCookie);

    return new HashMap<>() {{
      put("AccessToken", tokens.get("AccessToken"));
    }};
  }

  @Statistics(USER)
  @PostMapping("/refresh")
  @ResponseStatus(OK)
  public Map<String, String> reissueMobile(@Valid @RequestHeader String Authorization) {
    return userBusinessService.reissueMobile(Authorization); // todo: check business logic
  }

  @Authorize
  @Statistics(USER)
  @PostMapping("/favorite-major")
  @ResponseStatus(OK)
  public void saveFavoriteMajor(@Authenticated Long userId, @Valid @RequestBody MajorRequest request) {
    userBusinessService.saveFavoriteMajor(userId, request);
  }

  @Authorize
  @Statistics(USER)
  @DeleteMapping("/favorite-major")
  @ResponseStatus(OK)
  public void deleteFavoriteMajor(@Authenticated Long userId, @RequestParam String majorType) {
    userBusinessService.deleteFavoriteMajor(userId, majorType);
  }

  @Authorize
  @Statistics(USER)
  @GetMapping("/favorite-major")
  @ResponseStatus(OK)
  public ResponseForm loadFavoriteMajors(@Authenticated Long userId) {
    var response = userBusinessService.executeFavoriteMajorLoad(userId);
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
  public List<LoadMyRestrictedReasonResponse> loadRestrictedReason(@Authenticated Long userId) {
    return userBusinessService.executeLoadRestrictedReason(userId);
  }

  @Authorize
  @Statistics(USER)
  @GetMapping("/blacklist-reason")
  @ResponseStatus(OK) // todo: BlacklistDomainControllerV2 동일한 API
  public List<LoadMyBlackListReasonResponse> loadBlacklistReason(@Authenticated Long userId) {
    return userBusinessService.executeLoadBlackListReason(userId);
  }

  @Authorize
  @Statistics(USER)
  @DeleteMapping
  @ResponseStatus(OK)
  public Map<String, Boolean> quit(@Authenticated Long userId, @Valid @RequestBody UserRequest.Quit request) {
    return userBusinessService.quit(userId, request.password());
  }
}

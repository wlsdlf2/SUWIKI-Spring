package usw.suwiki.api.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import usw.suwiki.common.response.ResponseForm;
import usw.suwiki.domain.user.service.AuthService;
import usw.suwiki.statistics.annotation.Statistics;

import java.util.HashMap;

import static org.springframework.http.HttpStatus.OK;
import static usw.suwiki.statistics.log.MonitorTarget.USER;

@RestController
@RequestMapping("/v2/refreshtoken")
@RequiredArgsConstructor
public class RefreshTokenControllerV2 {
  private final AuthService authService;

  @Statistics(USER)
  @PostMapping("/web-client/refresh")
  @ResponseStatus(OK)
  public ResponseForm clientTokenRefresh(
    @CookieValue(value = "refreshToken") Cookie cookie,
    HttpServletResponse response
  ) {
    var token = authService.reissue(cookie.getValue());

    Cookie refreshCookie = new Cookie("refreshToken", token.getRefreshToken());
    refreshCookie.setMaxAge(14 * 24 * 60 * 60);
    refreshCookie.setSecure(true);
    refreshCookie.setHttpOnly(true);
    response.addCookie(refreshCookie);

    return ResponseForm.success(new HashMap<>() {{
      put("AccessToken", token.getAccessToken());
    }});
  }

  @Statistics(USER)
  @PostMapping("/mobile-client/refresh")
  @ResponseStatus(OK)
  public ResponseForm tokenRefresh(@RequestHeader String Authorization) {
    return ResponseForm.success(authService.reissue(Authorization));
  }
}

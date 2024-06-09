package usw.suwiki.auth.core.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import usw.suwiki.auth.core.annotation.Authorize;
import usw.suwiki.auth.token.jwt.JwtParser;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.domain.user.Role;

import static usw.suwiki.core.exception.ExceptionCode.USER_RESTRICTED;

@Component
@RequiredArgsConstructor
public class AuthorizationInterceptor implements HandlerInterceptor {
  private final JwtParser jwtParser;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    var authorize = resolve(handler);

    if (authorize != null) {
      String token = request.getHeader(HttpHeaders.AUTHORIZATION);
      validateAccessIfAdmin(authorize.value(), token);
      setAuthentication(token);
    }

    return true;
  }

  private Authorize resolve(Object handler) {
    return handler instanceof HandlerMethod handlerMethod ? handlerMethod.getMethodAnnotation(Authorize.class) : null;
  }

  private void validateAccessIfAdmin(Role request, String token) {
    if (request.isAdmin() && jwtParser.isNotAdmin(token)) {
      throw new AccountException(USER_RESTRICTED);
    }
  }

  private void setAuthentication(String token) {
    var authentication = jwtParser.parseAuthentication(token);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}

package usw.suwiki.statistics.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

final class ServletUtil {

  public static HttpServletRequest getHttpServletRequest() {
    var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return Objects.requireNonNull(attributes).getRequest();
  }
}

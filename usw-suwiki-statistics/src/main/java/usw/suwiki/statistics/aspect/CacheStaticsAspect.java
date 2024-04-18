package usw.suwiki.statistics.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Profile({"prod", "dev", "local"})
public class CacheStaticsAspect {

  private final HttpServletRequest httpServletRequest;
  private final CacheStaticsLogger cacheStaticsLogger;

  @Around("@annotation(usw.suwiki.statistics.annotation.CacheStatics)")
  public Object execute(ProceedingJoinPoint pjp) throws Throwable {
    cacheStaticsLogger.getCachesStats(httpServletRequest.getRequestURI().split("/")[1]);

    return pjp.proceed(pjp.getArgs());
  }
}

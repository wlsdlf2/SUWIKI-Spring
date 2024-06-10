package usw.suwiki.statistics.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import usw.suwiki.statistics.profile.AppProfile;

@Aspect
@Component
@AppProfile
@RequiredArgsConstructor
public class CacheStaticsAspect {
  private final CacheStaticsLogger cacheStaticsLogger;

  @Around("@annotation(usw.suwiki.statistics.annotation.CacheStatics)")
  public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
    var httpServletRequest = ServletUtil.getHttpServletRequest();
    cacheStaticsLogger.getCachesStats(httpServletRequest.getRequestURI().split("/")[1]);
    return joinPoint.proceed(joinPoint.getArgs());
  }
}

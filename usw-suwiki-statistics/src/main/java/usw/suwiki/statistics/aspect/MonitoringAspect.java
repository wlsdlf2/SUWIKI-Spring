package usw.suwiki.statistics.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import usw.suwiki.statistics.annotation.Statistics;
import usw.suwiki.statistics.log.ApiLoggerService;
import usw.suwiki.statistics.profile.AppProfile;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@AppProfile
@RequiredArgsConstructor
public class MonitoringAspect {
  private final ApiLoggerService apiLoggerService;

  @Around("@annotation(usw.suwiki.statistics.annotation.Statistics) && @annotation(statistics)")
  public Object monitor(ProceedingJoinPoint joinPoint, Statistics statistics) throws Throwable {
    LocalDateTime start = LocalDateTime.now();

    try {
      return joinPoint.proceed();
    } finally {
      var request = ServletUtil.getHttpServletRequest();

      LocalDateTime end = LocalDateTime.now();
      log.info("{} Api Start = {}, End = {}", request.getRequestURI(), start, end);

      apiLoggerService.logApi(statistics.value(), Duration.between(start, end).toMillis());
    }
  }
}

package usw.suwiki.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import usw.suwiki.common.event.ExceptionNotifier;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableAsync(proxyTargetClass = true)
public class AsyncConfig implements AsyncConfigurer {
  private final ExceptionNotifier notifier;

  @Override
  public Executor getAsyncExecutor() {
    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(3);
    executor.setMaxPoolSize(30);
    executor.setQueueCapacity(90);
    executor.setThreadNamePrefix("ASYNC-");
    executor.initialize();
    return executor;
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return (exception, method, params) -> {
      log.warn("[Async Exception] {} Occurs : {}", exception.getClass().getSimpleName(), exception.getMessage());
      notifier.notify(exception);
    };
  }
}

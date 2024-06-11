package usw.suwiki.core.advice;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import usw.suwiki.common.event.ExceptionNotifier;

@Component
@RequiredArgsConstructor
class ExceptionNotifierImpl implements ExceptionNotifier {
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public void notify(Throwable throwable) {
    eventPublisher.publishEvent(new UnexpectedExceptionEvent(throwable));
  }
}

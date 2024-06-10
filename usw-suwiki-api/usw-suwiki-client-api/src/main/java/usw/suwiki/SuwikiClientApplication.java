package usw.suwiki;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import usw.suwiki.common.event.Event;

@SpringBootApplication
@RequiredArgsConstructor
@ConfigurationPropertiesScan("usw.suwiki")
public class SuwikiClientApplication {
  private final ApplicationEventPublisher eventPublisher;

  @EventListener(ApplicationReadyEvent.class)
  private void start() {
    eventPublisher.publishEvent(new Event.Webhook("Client Application Started"));
  }

  public static void main(String[] args) {
    SpringApplication.run(SuwikiClientApplication.class, args);
  }
}

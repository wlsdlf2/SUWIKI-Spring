package usw.suwiki.statistics.webhook;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import usw.suwiki.common.event.Event;

@Component
@RequiredArgsConstructor
class WebhookEventListener {
  private final WebhookProperties webhookProperties;
  private final WebClient webClient;

  @EventListener
  public void onWebhookGeneralEvent(Event.Webhook event) {
    webClient.post()
      .uri(webhookProperties.getGeneralUri())
      .bodyValue(event.getMessage())
      .retrieve()
      .bodyToMono(Void.class)
      .subscribe();
  }
}

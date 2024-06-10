package usw.suwiki.statistics.webhook;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@Getter
@ConfigurationProperties(prefix = "webhook.discord")
class WebhookProperties {
  private final URI generalUri;
  private final URI errorUri;

  WebhookProperties(String general, String error) {
    this.generalUri = URI.create(general);
    this.errorUri = URI.create(error);
  }
}

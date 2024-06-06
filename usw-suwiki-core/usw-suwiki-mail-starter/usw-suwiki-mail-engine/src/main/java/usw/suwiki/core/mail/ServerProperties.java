package usw.suwiki.core.mail;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "server")
class ServerProperties {
  private static final String CONFIRM_URL_TEMPLATE = "%s/v2/confirmation-token/verify?token=%s";

  private final int port;
  private final String domain;

  String redirectUrl(String token) {
    return CONFIRM_URL_TEMPLATE.formatted(domain, token);
  }
}

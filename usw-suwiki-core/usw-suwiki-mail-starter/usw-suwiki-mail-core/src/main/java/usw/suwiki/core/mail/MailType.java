package usw.suwiki.core.mail;

import lombok.RequiredArgsConstructor;
import usw.suwiki.core.exception.MailException;

import java.util.List;

import static java.util.Collections.emptyList;
import static usw.suwiki.core.exception.ExceptionCode.BAD_MAIL_REQUEST;

@RequiredArgsConstructor
public enum MailType {
  FIND_ID(List.of("loginId")),
  EMAIL_AUTH(List.of("redirectUrl")),
  FIND_PASSWORD(List.of("password")),

  DELETE_WARNING(emptyList()),
  DORMANT_NOTIFICATION(emptyList()),
  PRIVACY_POLICY_NOTIFICATION(emptyList()),
  ;

  private final List<String> keys;

  public String template() {
    return this.name().replace("_", "-");
  }

  public String key() {
    if (keys.isEmpty()) {
      throw new MailException(BAD_MAIL_REQUEST);
    }

    return keys.get(0);
  }

  public boolean isEmailAuth() {
    return this == EMAIL_AUTH;
  }
}

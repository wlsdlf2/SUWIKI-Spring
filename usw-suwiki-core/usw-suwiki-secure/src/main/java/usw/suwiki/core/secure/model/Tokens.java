package usw.suwiki.core.secure.model;

import lombok.Data;

@Data
public final class Tokens {
  private final String accessToken;
  private final String refreshToken;
}

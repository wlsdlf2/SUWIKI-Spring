package usw.suwiki.auth.core.encode;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import usw.suwiki.core.secure.Encoder;

@Component
class DefaultEncoder implements Encoder {
  private final BCryptPasswordEncoder passwordEncoder;

  public DefaultEncoder() {
    this.passwordEncoder = new BCryptPasswordEncoder();
  }

  @Override
  public String encode(String input) {
    return passwordEncoder.encode(input);
  }

  @Override
  public boolean matches(CharSequence rawInput, String encoded) {
    return passwordEncoder.matches(rawInput, encoded);
  }

  @Override
  public boolean nonMatches(CharSequence rawInput, String encoded) {
    return !matches(rawInput, encoded);
  }
}

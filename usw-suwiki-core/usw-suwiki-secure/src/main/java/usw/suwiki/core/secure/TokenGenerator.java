package usw.suwiki.core.secure;

import usw.suwiki.core.secure.model.Claim;
import usw.suwiki.core.secure.model.Tokens;

/**
 * return set of jwts.
 *
 * @author hejow
 */
public interface TokenGenerator {
  Tokens login(Long userId, Claim claim);

  Tokens reissue(String refreshToken);
}

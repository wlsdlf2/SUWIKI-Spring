package usw.suwiki.core.secure;

import usw.suwiki.core.secure.model.Claim;

public interface TokenAgent {
  /**
   * @return refreshToken
   */
  String login(Long userId);

  /**
   * @param payload refreshToken
   * @return refreshToken
   */
  String reissue(String payload);

  String createAccessToken(Long userId, Claim claim);
}

package usw.suwiki.auth.token.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.domain.user.Role;

import static java.lang.Boolean.TRUE;
import static usw.suwiki.auth.token.jwt.RawParser.Content;
import static usw.suwiki.core.exception.ExceptionCode.USER_RESTRICTED;

@Component
@RequiredArgsConstructor
public class JwtParser {
  private final UserDetailsService userDetailsService;
  private final RawParser rawParser;

  public Authentication parseAuthentication(String token) {
    validateRestricted(token);
    var loginId = rawParser.parse(token, Content.LOGIN_ID, String.class);
    var userDetails = userDetailsService.loadUserByUsername(loginId);
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }

  private void validateRestricted(String token) {
    if (TRUE.equals(rawParser.parse(token, Content.RESTRICTED, Boolean.class))) {
      throw new AccountException(USER_RESTRICTED);
    }
  }

  public boolean isNotAdmin(String token) {
    var role = rawParser.parse(token, Content.ROLE, String.class);
    return token != null && !Role.isAdmin(role);
  }
}

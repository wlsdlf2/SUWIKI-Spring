package usw.suwiki.auth.core.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;
import usw.suwiki.core.exception.AccountException;

import static usw.suwiki.core.exception.ExceptionCode.EXPIRED_TOKEN;
import static usw.suwiki.core.exception.ExceptionCode.INVALID_TOKEN;
import static usw.suwiki.core.exception.ExceptionCode.LOGIN_REQUIRED;

@Component
class RawParser {
  enum Content {
    ID, LOGIN_ID, ROLE, RESTRICTED;
  }

  private final JwtParser parser;

  RawParser(JwtSecretProvider jwtSecretProvider) {
    this.parser = Jwts.parserBuilder()
      .setSigningKey(jwtSecretProvider.key())
      .build();
  }

  Jws<Claims> validate(String token) {
    try {
      return parser.parseClaimsJws(token);
    } catch (MalformedJwtException | IllegalArgumentException exception) {
      throw new AccountException(LOGIN_REQUIRED);
    } catch (ExpiredJwtException exception) {
      throw new AccountException(EXPIRED_TOKEN);
    } catch (SignatureException exception) {
      throw new AccountException(INVALID_TOKEN);
    }
  }

  <T> T parse(String jwt, Content content, Class<T> type) {
    return validate(jwt).getBody()
      .get(content.name(), type);
  }

  boolean isExpired(String jwt) {
    try {
      parser.parseClaimsJws(jwt).getBody().getExpiration();
    } catch (ExpiredJwtException expiredJwtException) {
      return true;
    }
    return false;
  }
}

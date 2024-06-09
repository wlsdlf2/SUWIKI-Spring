package usw.suwiki.auth.token.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import usw.suwiki.auth.service.RefreshTokenService;
import usw.suwiki.core.secure.TokenGenerator;
import usw.suwiki.core.secure.model.Claim;
import usw.suwiki.core.secure.model.Tokens;
import usw.suwiki.domain.user.service.UserService;

import java.util.Map;

import static usw.suwiki.auth.token.jwt.RawParser.Content;

@Component
@RequiredArgsConstructor
public class JwtGenerator implements TokenGenerator {
  private final RefreshTokenService refreshTokenService;
  private final UserService userService;

  private final JwtSecretProvider jwtSecretProvider;

  @Override
  public Tokens login(Long userId, Claim claim) {
    return new Tokens(generateAccessToken(userId, claim), refreshTokenService.issue(userId));
  }

  @Override
  public Tokens reissue(String payload) {
    var refreshToken = refreshTokenService.reissue(payload);
    var userId = refreshTokenService.parseUserId(payload);
    var user = userService.loadById(userId); // todo (06.09) 추후 한번의 로직으로 개선하기 (불필요한 userService 의존 등)

    return new Tokens(generateAccessToken(userId, user.toClaim()), refreshToken);
  }

  private String generateAccessToken(Long userId, Claim claim) {
    Claims claims = Jwts.claims().setSubject(claim.loginId());
    claims.putAll(Map.of(
      Content.ID.name(), userId,
      Content.LOGIN_ID.name(), claim.loginId(),
      Content.ROLE.name(), claim.role(),
      Content.RESTRICTED.name(), claim.restricted()
    ));

    return Jwts.builder()
      .signWith(jwtSecretProvider.key())
      .setHeaderParam("type", "JWT")
      .setClaims(claims)
      .setExpiration(jwtSecretProvider.accessTokenExpiredTime())
      .compact();
  }
}

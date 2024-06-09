package usw.suwiki.auth.service;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.auth.token.RefreshToken;
import usw.suwiki.auth.token.RefreshTokenRepository;
import usw.suwiki.auth.token.jwt.JwtSecretProvider;
import usw.suwiki.auth.token.jwt.RawParser;
import usw.suwiki.core.exception.AccountException;

import static usw.suwiki.core.exception.ExceptionCode.INVALID_TOKEN;

@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;

  private final JwtSecretProvider jwtSecretProvider;
  private final RawParser rawParser;

  public void deleteByUserId(Long userId) {
    refreshTokenRepository.deleteByUserIdx(userId);
  }

  public String issue(Long userId) {
    return refreshTokenRepository.findByUserIdx(userId)
      .map(this::getPayload)
      .orElseGet(() -> generate(userId).getPayload());
  }

  private String getPayload(RefreshToken refreshToken) {
    return rawParser.isExpired(refreshToken.getPayload()) ? refreshToken.reissue(generateRefreshToken()) : refreshToken.getPayload();
  }

  private RefreshToken generate(Long userId) {
    var refreshToken = new RefreshToken(userId, generateRefreshToken());
    return refreshTokenRepository.save(refreshToken);
  }

  public String reissue(String payload) {
    var refreshToken = loadByPayload(payload);
    refreshToken.validatePayload(payload);
    return refreshToken.reissue(generateRefreshToken());
  }

  public Long parseUserId(String payload) {
    var refreshToken = loadByPayload(payload);
    return refreshToken.getUserIdx();
  }

  private RefreshToken loadByPayload(String payload) {
    return refreshTokenRepository.findByPayload(payload)
      .orElseThrow(() -> new AccountException(INVALID_TOKEN));
  }

  private String generateRefreshToken() {
    return Jwts.builder()
      .signWith(jwtSecretProvider.key())
      .setHeaderParam("type", "JWT")
      .setExpiration(jwtSecretProvider.refreshTokenExpireTime())
      .compact();
  }
}

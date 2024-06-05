package usw.suwiki.auth.token;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.infra.jpa.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

import static usw.suwiki.core.exception.ExceptionCode.EMAIL_NOT_AUTHED;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "createDate", column = @Column(name = "createdAt", nullable = false))
public class ConfirmationToken extends BaseEntity {
  private static final long CONFIRM_PERIOD = 15;

  @Column
  private Long userIdx;

  @Column(nullable = false)
  private String token;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  @Column
  private LocalDateTime confirmedAt;

  public ConfirmationToken(Long userId) {
    this.userIdx = userId;
    this.token = UUID.randomUUID().toString();
    this.expiresAt = LocalDateTime.now().plusMinutes(CONFIRM_PERIOD);
  }

  public void confirm() {
    this.confirmedAt = LocalDateTime.now();
  }

  public boolean isExpired() {
    return this.expiresAt.isBefore(LocalDateTime.now());
  }

  public void validateVerified() {
    if (this.confirmedAt == null) {
      throw new AccountException(EMAIL_NOT_AUTHED);
    }
  }
}

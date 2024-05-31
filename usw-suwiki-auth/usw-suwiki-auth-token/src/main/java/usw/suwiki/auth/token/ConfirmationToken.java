package usw.suwiki.auth.token;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import usw.suwiki.infra.jpa.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "createDate", column = @Column(name = "createdAt", nullable = false))
public class ConfirmationToken extends BaseEntity {
  @Column
  private Long userIdx;

  @Column(nullable = false)
  private String token;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  @Column
  private LocalDateTime confirmedAt;

  public ConfirmationToken(Long userIdx) {
    this.userIdx = userIdx;
    this.token = UUID.randomUUID().toString();
    this.expiresAt = LocalDateTime.now().plusMinutes(15);
  }

  public ConfirmationToken confirm() {
    this.confirmedAt = LocalDateTime.now();
    return this;
  }

  public boolean isExpired() {
    return this.expiresAt.isBefore(LocalDateTime.now());
  }

  public boolean isVerified() {
    return this.confirmedAt != null;
  }
}

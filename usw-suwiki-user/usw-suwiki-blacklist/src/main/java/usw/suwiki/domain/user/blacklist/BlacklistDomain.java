package usw.suwiki.domain.user.blacklist;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import usw.suwiki.infra.jpa.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "createDate", column = @Column(name = "created_at"))
@AttributeOverride(name = "modifiedDate", column = @Column(name = "updated_at"))
public class BlacklistDomain extends BaseEntity {
  private static final String OVER_RESTRICTED_REASON = "신고 누적으로 인한 블랙리스트";
  private static final String OVER_RESTRICTED_JUDGEMENT = "신고누적 블랙리스트 1년";

  private static final long OVER_RESTRICTED_BANNED_PERIOD = 365L;
  private static final long PERMANENT_BAN = 9999L;

  @Column
  private Long userIdx;

  @Column
  private String hashedEmail;

  @Column(name = "banned_reason")
  private String reason;

  @Column
  private String judgement;

  @Column
  private LocalDateTime expiredAt;

  public static BlacklistDomain permanent(Long userId, String encodedEmail, String reason, String judgement) {
    return new BlacklistDomain(userId, encodedEmail, reason, judgement, LocalDateTime.now().plusMonths(PERMANENT_BAN));
  }

  public static BlacklistDomain overRestrict(Long userId, String encodedEmail) {
    return new BlacklistDomain(userId, encodedEmail, OVER_RESTRICTED_REASON, OVER_RESTRICTED_JUDGEMENT, LocalDateTime.now().plusDays(OVER_RESTRICTED_BANNED_PERIOD));
  }
}

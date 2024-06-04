package usw.suwiki.domain.user.restricted;


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
public class RestrictingUser extends BaseEntity {
  @Column
  private Long userIdx;

  @Column
  private LocalDateTime restrictingDate;

  @Column(name = "restricting_reason")
  private String reason;

  @Column
  private String judgement;

  public static RestrictingUser of(Long userId, long banPeriod, String reason, String judgement) {
    return new RestrictingUser(userId, LocalDateTime.now().plusDays(banPeriod), reason, judgement);
  }
}

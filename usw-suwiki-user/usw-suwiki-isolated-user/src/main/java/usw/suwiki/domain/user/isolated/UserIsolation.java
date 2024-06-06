package usw.suwiki.domain.user.isolated;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.core.secure.Encoder;
import usw.suwiki.domain.user.User;
import usw.suwiki.infra.jpa.BaseEntity;

import java.time.LocalDateTime;

import static usw.suwiki.core.exception.ExceptionCode.LOGIN_FAIL;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserIsolation extends BaseEntity {
  @Column
  private Long userIdx;

  @Column
  private String loginId;

  @Column
  private String password;

  @Column
  private String email;

  @Column
  private LocalDateTime requestedQuitDate;

  @Column
  private LocalDateTime lastLogin;

  public static UserIsolation from(User user) {
    return new UserIsolation(user.getId(), user.getLoginId(), user.getPassword(), user.getEmail(), user.getLastLogin(), user.getRequestedQuitDate());
  }

  public void validateLoginable(Encoder encoder, String inputPassword) {
    if (encoder.nonMatches(inputPassword, password)) {
      throw new AccountException(LOGIN_FAIL);
    }
  }
}

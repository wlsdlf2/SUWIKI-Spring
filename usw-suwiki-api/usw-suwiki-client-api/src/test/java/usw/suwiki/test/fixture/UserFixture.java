package usw.suwiki.test.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import usw.suwiki.domain.user.Role;
import usw.suwiki.domain.user.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserFixture {

  public static User unconfirmed() {
    return User.join("loginId", "password", "test@suwiki.kr");
  }

  public static User anotherUnconfirmed() {
    return User.join("loginId2", "password", "test2@suwiki.kr");
  }

  public static User one() {
    return unconfirmed().activate();
  }

  public static User another() {
    return anotherUnconfirmed().activate();
  }

  public static User admin() {
    return User.builder()
      .loginId("admin")
      .password("password")
      .email("admin@suwiki.kr")
      .role(Role.ADMIN)
      .restricted(false)
      .build();
  }
}

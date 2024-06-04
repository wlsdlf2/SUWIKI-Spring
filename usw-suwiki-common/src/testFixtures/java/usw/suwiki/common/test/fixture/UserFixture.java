package usw.suwiki.common.test.fixture;

import usw.suwiki.domain.user.Role;
import usw.suwiki.domain.user.User;

public class UserFixture {

  private UserFixture() {
  }

  public static User one(String loginId, String password) {
    return User.join(
      loginId == null ? "loginId" : loginId,
      password == null ? "password" : password,
      "test@suwiki.kr"
    ).activate();
  }

  public static User another() {
    return User.join("loginId2", "password", "test2@suwiki.kr").activate();
  }

  public static User admin(String loginId, String password) {
    return User.builder()
      .loginId(loginId == null ? "admin" : loginId)
      .password(password == null ? "password" : password)
      .email("admin@suwiki.kr")
      .role(Role.ADMIN)
      .restricted(false)
      .build();
  }
}

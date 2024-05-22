package usw.suwiki.api.user;

import usw.suwiki.domain.user.User;

public final class UserFixture {

  public static User unconfirmed() {
    return User.init("loginId", "password", "test@suwiki.kr");
  }

  public static User anotherUnconfirmed() {
    return User.init("loginId2", "password", "test2@suwiki.kr");
  }

  public static User one() {
    return unconfirmed().activate();
  }

  public static User another() {
    return anotherUnconfirmed().activate();
  }
}

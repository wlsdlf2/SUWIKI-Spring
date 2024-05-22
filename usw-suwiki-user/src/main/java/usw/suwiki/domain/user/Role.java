package usw.suwiki.domain.user;

public enum Role {
  ADMIN,
  USER,
  ;

  public boolean isAdmin() {
    return this.equals(ADMIN);
  }

  public boolean isAdmin(String key) {
    return ADMIN.name().equals(key.toUpperCase());
  }
}

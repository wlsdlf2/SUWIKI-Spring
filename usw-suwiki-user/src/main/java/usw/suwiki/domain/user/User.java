package usw.suwiki.domain.user;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.core.secure.Encoder;
import usw.suwiki.core.secure.RandomPasswordGenerator;
import usw.suwiki.domain.user.model.UserAdapter;
import usw.suwiki.domain.user.model.UserClaim;
import usw.suwiki.infra.jpa.BaseEntity;

import java.time.LocalDateTime;
import java.util.Objects;

import static usw.suwiki.core.exception.ExceptionCode.OUT_OF_POINT;
import static usw.suwiki.core.exception.ExceptionCode.PASSWORD_ERROR;
import static usw.suwiki.core.exception.ExceptionCode.SAME_PASSWORD_WITH_OLD;
import static usw.suwiki.core.exception.ExceptionCode.USER_NOT_FOUND;

@Entity
@Getter
@Builder
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "createDate", column = @Column(name = "created_at"))
@AttributeOverride(name = "modifiedDate", column = @Column(name = "updated_at"))
public class User extends BaseEntity {
  private static final int DELETE_POINT_LIMIT = 30;
  private static final int PURCHASE_POINT_LIMIT = 20;
  private static final int WRITE_EXAM_POST_POINT = 20;
  private static final int WRITE_EVALUATION_POINT = 10;
  private static final int ARREST_LIMIT = 2;

  // todo: 로그인 아이디와 이메일은 고유값임에도 불구하고 db에 unique가 빠져있다.
  @Column
  private String loginId;

  @Column
  private String password;

  @Column
  private String email;

  @Column
  private boolean restricted;

  @Column
  private Integer restrictedCount;

  @Enumerated(EnumType.STRING)
  private Role role;

  @Column
  private Integer writtenEvaluation;

  @Column
  private Integer writtenExam;

  @Column
  private Integer viewExamCount;

  @Column
  private Integer point;

  @Column
  private LocalDateTime lastLogin;

  @Column
  private LocalDateTime requestedQuitDate;

  public static User join(String loginId, String password, String email) {
    return builder()
      .loginId(loginId)
      .password(password)
      .email(email)
      .restricted(true)
      .restrictedCount(0)
      .writtenEvaluation(0)
      .writtenExam(0)
      .point(0)
      .viewExamCount(0)
      .build();
  }

  public void restrict() {
    this.restricted = true;
  }

  public void released() {
    this.restricted = false;
  }

  public void waitQuit() {
    this.restrictedCount = null;
    this.role = null;
    this.writtenExam = null;
    this.writtenEvaluation = null;
    this.viewExamCount = null;
    this.point = null;
    this.lastLogin = null;
    this.requestedQuitDate = LocalDateTime.now();
    restrict();
  }

  public void sleep() {
    this.loginId = null;
    this.password = null;
    this.email = null;
  }

  public void wake(String loginId, String password, String email) {
    this.loginId = loginId;
    this.password = password;
    this.email = email;
    login();
  }

  public User activate() {
    this.restricted = false;
    this.role = Role.USER;
    super.modified(); // todo: jpa dirty checking 테스트하고 불필요하면 지우기
    return this;
  }

  public UserAdapter toAdapter() { // todo: 네이밍 수정하기 (Adapter가 용도를 잘 드러내지 않음)
    return new UserAdapter(this.getId(), this.loginId, this.role);
  }

  public UserClaim toClaim() { // 토큰을 생성하기 위한 유저 정보, 강하게 결합되어있어 User 에게 위임
    return new UserClaim(this.loginId, this.role.name(), this.restricted);
  }

  public boolean isAdmin() {
    return this.role.isAdmin();
  }

  public void validateEmail(String email) {
    if (!Objects.equals(this.email, email)) {
      throw new AccountException(USER_NOT_FOUND);
    }
  }

  public boolean isPasswordEquals(Encoder encoder, String rawPassword) {
    return encoder.matches(rawPassword, this.password);
  }

  public void validatePassword(Encoder encoder, String rawPassword) {
    if (!isPasswordEquals(encoder, rawPassword)) {
      throw new AccountException(PASSWORD_ERROR);
    }
  }

  private void validateDuplicatedPassword(Encoder encoder, String newPassword) {
    if (isPasswordEquals(encoder, newPassword)) {
      throw new AccountException(SAME_PASSWORD_WITH_OLD);
    }
  }

  public void changePassword(Encoder encoder, String prePassword, String newPassword) {
    validatePassword(encoder, prePassword);
    validateDuplicatedPassword(encoder, newPassword);
    this.password = encoder.encode(newPassword);
  }

  public String resetPassword(Encoder encoder) {
    String newPassword = RandomPasswordGenerator.generate();
    this.password = encoder.encode(newPassword);
    return password;
  }

  public void login() {
    this.lastLogin = LocalDateTime.now();
  }

  public void evaluate() {
    this.point += WRITE_EVALUATION_POINT;
    this.writtenEvaluation++;
  }

  public void eraseEvaluation() {
    validatePointLimit(PURCHASE_POINT_LIMIT);
    this.point -= DELETE_POINT_LIMIT;
    this.writtenEvaluation--;
  }

  public void writeExamPost() {
    this.point += WRITE_EXAM_POST_POINT;
    this.writtenExam++;
  }

  public void purchaseExamPost() {
    validatePointLimit(PURCHASE_POINT_LIMIT);
    this.point -= PURCHASE_POINT_LIMIT;
    this.viewExamCount++;
  }

  public void eraseExamPost() {
    validatePointLimit(DELETE_POINT_LIMIT);
    this.point -= DELETE_POINT_LIMIT;
    this.writtenExam--;
  }

  private void validatePointLimit(int required) {
    if (this.point < required) {
      throw new AccountException(OUT_OF_POINT);
    }
  }

  public void reported() {
    this.restrictedCount++;
    restrict();
  }

  public void report() {
    this.point++;
  }

  public boolean isArrestable() {
    return this.restrictedCount >= ARREST_LIMIT;
  }
}

package usw.suwiki.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserRequest {

  public record CheckLoginId(@NotBlank String loginId) {
  }

  public record CheckEmail(@NotBlank String email) {
  }

  public record Join(
    @NotBlank String loginId,
    @NotBlank String password,
    @NotBlank String email
  ) {
  }

  public record Login(
    @NotBlank String loginId,
    @NotBlank String password
  ) {
  }

  public record FindId(@NotBlank String email) {
  }

  public record FindPassword(
    @NotBlank String loginId,
    @NotBlank String email
  ) {
  }

  public record EditPassword(
    @NotBlank String prePassword,
    @NotBlank String newPassword
  ) {
  }

  public record Quit(
    @NotBlank String loginId,
    @NotBlank String password
  ) {
  }
}

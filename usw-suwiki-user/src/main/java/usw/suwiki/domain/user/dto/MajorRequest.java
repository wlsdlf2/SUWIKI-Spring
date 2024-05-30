package usw.suwiki.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MajorRequest {
  @NotBlank
  private String majorType;
}

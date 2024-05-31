package usw.suwiki.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // for jackson
@AllArgsConstructor
public class MajorRequest {
  @NotBlank
  private String majorType;
}

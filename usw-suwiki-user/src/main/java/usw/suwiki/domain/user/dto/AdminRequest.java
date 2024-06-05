package usw.suwiki.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class AdminRequest {

  public record EvaluatePostNoProblem(
    @NotNull Long evaluateIdx
  ) {
  }

  public record ExamPostNoProblem(
    @NotNull Long examIdx
  ) {
  }

  public record RestrictEvaluatePost(
    @NotNull Long evaluateIdx,
    @NotNull Long restrictingDate,
    @NotBlank String restrictingReason,
    @NotBlank String judgement
  ) {
  }

  public record RestrictExamPost(
    @NotNull Long examIdx,
    @NotNull Long restrictingDate,
    @NotBlank String restrictingReason,
    @NotBlank String judgement
  ) {
  }

  public record EvaluatePostBlacklist(
    @NotNull Long evaluateIdx,
    @NotBlank String bannedReason,
    @NotBlank String judgement
  ) {
  }

  public record ExamPostBlacklist(
    @NotNull Long examIdx,
    @NotBlank String bannedReason,
    @NotBlank String judgement
  ) {
  }
}

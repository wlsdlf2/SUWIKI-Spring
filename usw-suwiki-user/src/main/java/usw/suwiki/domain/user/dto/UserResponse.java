package usw.suwiki.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResponse {

  @Data
  public static class Success { // legacy
    private final boolean success;
  }

  @Data
  public static class Overlap { // legacy
    private final boolean overlap;
  }

  public record MyPage(
    String loginId,
    String email,
    Integer point,
    Integer writtenEvaluation,
    Integer writtenExam,
    Integer viewExam
  ) {
  }

  public record RestrictedReason(
    String restrictedReason,
    String judgement,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    LocalDateTime createdAt,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    LocalDateTime restrictingDate
  ) {
  }

  public record BlackedReason(
    String blackListReason,
    String judgement,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    LocalDateTime createdAt,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    LocalDateTime expiredAt
  ) {
  }
}

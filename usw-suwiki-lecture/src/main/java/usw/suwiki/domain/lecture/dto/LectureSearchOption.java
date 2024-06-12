package usw.suwiki.domain.lecture.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LectureSearchOption {
  private final String order;
  private final Long page;
  private final String major;

  public boolean isAllMajor() {
    return "전체".equals(major);
  }
}

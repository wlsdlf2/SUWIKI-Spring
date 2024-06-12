package usw.suwiki.domain.lecture.dto;

import lombok.Getter;

@Getter
public class LectureSearchOption {
  private static final String DEFAULT_ORDER = "date";
  private static final long DEFAULT_PAGE = 1L;

  private final String order;
  private final Long page;
  private final String major;

  public LectureSearchOption(String order, Long page, String major) {
    this.order = order == null ? DEFAULT_ORDER : order;
    this.page = page;
    this.major = major;
  }

  public long getPage() {
    return (this.page == null ? DEFAULT_PAGE : this.page) - 1;
  }
}

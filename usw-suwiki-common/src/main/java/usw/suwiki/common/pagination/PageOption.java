package usw.suwiki.common.pagination;

import lombok.Getter;

import java.util.Optional;

@Getter
public class PageOption {

  private final int page;

  private PageOption(Optional<Integer> page) {
    this.page = page.orElse(1);
  }

  public static int offset(Optional<Integer> page) {
    return new PageOption(page).getOffset();
  }

  private int getOffset() {
    return (page - 1) * 10;
  }
}

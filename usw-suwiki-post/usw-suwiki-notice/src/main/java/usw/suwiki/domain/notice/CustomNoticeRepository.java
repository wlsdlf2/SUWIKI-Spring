package usw.suwiki.domain.notice;

import usw.suwiki.common.pagination.PageOption;

import java.util.List;

public interface CustomNoticeRepository {
  List<Notice> findByNoticeList(PageOption page);
}

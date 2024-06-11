package usw.suwiki.domain.notice;

import java.util.List;

public interface CustomNoticeRepository {
  List<Notice> findByNoticeList(int offset);
}

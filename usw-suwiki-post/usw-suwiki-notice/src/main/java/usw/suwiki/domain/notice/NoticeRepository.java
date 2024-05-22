package usw.suwiki.domain.notice;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long>, CustomNoticeRepository {
}

package usw.suwiki.domain.notice.persist;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import usw.suwiki.common.pagination.PageOption;
import usw.suwiki.domain.notice.CustomNoticeRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
class CustomNoticeRepositoryImpl implements CustomNoticeRepository {
  private final EntityManager em;

  @Override
  public List findByNoticeList(PageOption page) {
    return em.createQuery("SELECT n from Notice n ORDER BY n.modifiedDate DESC")
      .setFirstResult((page.getPageNumber().get()) * 10)
      .setMaxResults(10)
      .getResultList();
  }
}


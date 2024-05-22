package usw.suwiki.domain.viewexam.persist;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import usw.suwiki.domain.viewexam.CustomViewExamRepository;
import usw.suwiki.domain.viewexam.ViewExam;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
class CustomViewExamRepositoryImpl implements CustomViewExamRepository {
  private final EntityManager em;

  @Override
  public boolean isExists(Long userId, Long lectureId) {
    return em.createQuery("""
            SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END
            FROM ViewExam v
            WHERE v.userIdx = :userId and v.lectureId = :lectureId
        """, Boolean.class)
      .setParameter("userId", userId)
      .setParameter("lectureId", lectureId)
      .getSingleResult();
  }

  @Override
  public List<ViewExam> findByUserId(Long userIdx) {
    List result = em.createQuery("SELECT v FROM ViewExam v JOIN v.userIdx u WHERE v.userIdx = :idx ORDER BY v.createDate")
      .setParameter("idx", userIdx)
      .getResultList();

    return result.isEmpty() ? Collections.emptyList() : result;
  }
}

package usw.suwiki.domain.lecture.major;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FavoriteMajorRepository {
  private final EntityManager em;

  public List<FavoriteMajor> findAllByUser(Long userIdx) {
    return em.createQuery("SELECT f from FavoriteMajor f where f.userIdx = :id")
      .setParameter("id", userIdx)
      .getResultList();
  }

  public List<String> findOnlyMajorTypeByUser(Long userIdx) {
    return em.createQuery("SELECT f.majorType from FavoriteMajor f where f.userIdx = :id")
      .setParameter("id", userIdx)
      .getResultList();
  }

  public FavoriteMajor findByUserAndMajorType(Long userIdx, String majorType) {
    List resultList = em.createQuery(
        "SELECT f from FavoriteMajor f where f.userIdx = :id AND f.majorType = :majorType")
      .setParameter("id", userIdx)
      .setParameter("majorType", majorType)
      .getResultList();

    return (FavoriteMajor) resultList.get(0);
  }

  public void save(FavoriteMajor favoriteMajor) {
    em.persist(favoriteMajor);
  }

  public void delete(FavoriteMajor favoriteMajor) {
    em.remove(favoriteMajor);
  }
}

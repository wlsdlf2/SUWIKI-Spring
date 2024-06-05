package usw.suwiki.domain.user.isolated;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserIsolationRepository extends JpaRepository<UserIsolation, Long> {
  boolean existsByLoginId(String loginId);

  boolean existsByEmail(String email);

  boolean existsByUserIdx(Long userIdx);

  boolean existsByLoginIdAndEmail(String loginId, String email);

  Optional<UserIsolation> findByUserIdx(Long userIdx);

  Optional<UserIsolation> findByLoginId(String loginId);

  Optional<UserIsolation> findByEmail(String email);

  void deleteByLoginId(String loginId);

  void deleteByUserIdx(Long userIdx);

  List<UserIsolation> findByRequestedQuitDateBefore(LocalDateTime localDateTime);
}

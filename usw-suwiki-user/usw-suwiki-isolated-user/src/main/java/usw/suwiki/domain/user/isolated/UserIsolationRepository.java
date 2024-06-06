package usw.suwiki.domain.user.isolated;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserIsolationRepository extends JpaRepository<UserIsolation, Long> {

  List<UserIsolation> findByRequestedQuitDateBefore(LocalDateTime localDateTime);

  Optional<UserIsolation> findByLoginIdAndEmail(String loginId, String email);

  Optional<UserIsolation> findByLoginId(String loginId);

  Optional<UserIsolation> findByEmail(String email);

  boolean existsByLoginId(String loginId);

  boolean existsByUserIdx(Long userIdx);

  boolean existsByEmail(String email);

  void deleteByUserIdx(Long userIdx);
}

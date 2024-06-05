package usw.suwiki.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  boolean existsByLoginId(String loginId);

  boolean existsByEmail(String email);

  Optional<User> findById(Long userId);

  Optional<User> findByLoginId(String loginId);

  Optional<User> findByEmail(String email);

  List<User> findByLastLoginBetween(LocalDateTime startTime, LocalDateTime endTime);

  List<User> findByRequestedQuitDateBefore(LocalDateTime localDateTime);
}

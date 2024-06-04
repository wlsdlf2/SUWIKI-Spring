package usw.suwiki.domain.user.restricted;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RestrictingUserRepository extends JpaRepository<RestrictingUser, Long> {

  List<RestrictingUser> findByRestrictingDateBefore(LocalDateTime localDateTime);

  Optional<RestrictingUser> findByUserIdx(Long userIdx);

  void deleteByUserIdx(Long userIdx);
}

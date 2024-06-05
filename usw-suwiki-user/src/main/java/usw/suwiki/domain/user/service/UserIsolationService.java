package usw.suwiki.domain.user.service;

import usw.suwiki.core.secure.Encoder;
import usw.suwiki.domain.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserIsolationService {
  boolean isSleeping(String loginId, String email);

  boolean isIsolatedByEmail(String email);

  boolean isIsolatedByLoginId(String loginId);

  long countAllIsolatedUsers();

  boolean isLoginable(String loginId, String inputPassword, Encoder encoder);

  Optional<String> findIsolatedLoginIdByEmail(String email);

  String updateIsolatedUserPassword(Encoder encoder, String email); // todo: refactoring 할 것

  User wake(UserService userService, String loginId);

  void saveFrom(User user);

  void deleteByUserId(Long userIdx);

  boolean isNotSleepingByUserId(Long userId);

  List<Long> loadAllIsolatedUntil(LocalDateTime target);
}

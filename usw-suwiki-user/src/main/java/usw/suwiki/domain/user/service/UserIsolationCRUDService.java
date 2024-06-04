package usw.suwiki.domain.user.service;

import usw.suwiki.core.secure.Encoder;
import usw.suwiki.domain.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserIsolationCRUDService {
  long countAllIsolatedUsers();

  boolean isIsolatedByEmail(String email);

  boolean isIsolatedByLoginId(String loginId);

  boolean isRetrievedUserEquals(String email, String loginId);

  boolean isLoginable(String loginId, String inputPassword, Encoder encoder);

  Optional<String> getIsolatedLoginIdByEmail(String email);

  String updateIsolatedUserPassword(Encoder encoder, String email); // todo: refactoring 할 것

  User wakeIsolated(UserCRUDService userCRUDService, String loginId);

  void saveUserIsolation(User user);

  void deleteByUserIdx(Long userIdx);

  boolean isNotIsolated(Long userId);

  List<Long> loadAllIsolatedUntilTarget(LocalDateTime target);
}

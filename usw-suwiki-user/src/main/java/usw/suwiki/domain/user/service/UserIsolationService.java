package usw.suwiki.domain.user.service;

import usw.suwiki.core.secure.Encoder;

import java.time.LocalDateTime;
import java.util.List;

public interface UserIsolationService {
  List<Long> loadAllIsolatedUntil(LocalDateTime target);

  long countAllIsolatedUsers();

  void isolate(Long userId);

  void wakeIfSleeping(String email);

  void wakeIfSleeping(String loginId, String email);

  void wakeIfSleeping(String loginId, Encoder encoder, String password);

  boolean isIsolatedByEmail(String email);

  boolean isIsolatedByLoginId(String loginId);

  void deleteByUserId(Long userIdx);
}

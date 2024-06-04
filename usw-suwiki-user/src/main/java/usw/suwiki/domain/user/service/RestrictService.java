package usw.suwiki.domain.user.service;

import usw.suwiki.domain.user.dto.UserResponse;

import java.util.List;

public interface RestrictService {
  List<UserResponse.RestrictedReason> loadRestrictedLog(Long userId);

  List<Long> loadAllRestrictedUntilNow();

  void release(Long userId);

  void restrict(Long reportedId, long banPeriod, String reason, String judgement);
}

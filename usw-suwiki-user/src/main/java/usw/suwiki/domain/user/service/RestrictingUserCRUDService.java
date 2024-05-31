package usw.suwiki.domain.user.service;

import java.util.List;

import static usw.suwiki.domain.user.dto.UserResponse.RestrictedReason;

public interface RestrictingUserCRUDService {
  List<RestrictedReason> loadRestrictedLog(Long userIdx);
}

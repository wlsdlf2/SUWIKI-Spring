package usw.suwiki.domain.user.service;

import java.util.List;

import static usw.suwiki.domain.user.dto.UserResponse.LoadMyRestrictedReasonResponse;

public interface RestrictingUserCRUDService {
  List<LoadMyRestrictedReasonResponse> loadRestrictedLog(Long userIdx);
}

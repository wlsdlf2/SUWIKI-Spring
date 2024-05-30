package usw.suwiki.domain.user.service;

import java.util.List;

import static usw.suwiki.domain.user.dto.UserResponse.LoadMyBlackListReasonResponse;

public interface BlacklistDomainCRUDService {
  List<LoadMyBlackListReasonResponse> loadAllBlacklistLog(Long userIdx);

  void saveBlackListDomain(Long userIdx, Long bannedPeriod, String bannedReason, String judgement);
}

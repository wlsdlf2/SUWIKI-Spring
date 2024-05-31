package usw.suwiki.domain.user.service;

import java.util.List;

import static usw.suwiki.domain.user.dto.UserResponse.BlackedReason;

public interface BlacklistDomainCRUDService {
  List<BlackedReason> loadAllBlacklistLog(Long userIdx);

  void saveBlackListDomain(Long userIdx, Long bannedPeriod, String bannedReason, String judgement);
}

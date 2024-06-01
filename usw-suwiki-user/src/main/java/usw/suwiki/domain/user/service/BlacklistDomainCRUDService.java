package usw.suwiki.domain.user.service;

import java.util.List;

import static usw.suwiki.domain.user.dto.UserResponse.BlackedReason;

public interface BlacklistDomainCRUDService {
  List<BlackedReason> loadAllBlacklistLogs(Long userIdx);

  void saveBlackListDomain(Long userIdx, Long bannedPeriod, String bannedReason, String judgement);
}

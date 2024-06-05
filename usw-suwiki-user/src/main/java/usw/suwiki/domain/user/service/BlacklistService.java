package usw.suwiki.domain.user.service;

import usw.suwiki.domain.user.dto.UserResponse;

import java.util.List;

public interface BlacklistService {

  List<UserResponse.BlackedReason> loadAllBlacklistLogs(Long userIdx);

  void validateNotBlack(String email);

  void black(Long userId, String email, String reason, String judgement);

  void overRestricted(Long userId, String email);
}

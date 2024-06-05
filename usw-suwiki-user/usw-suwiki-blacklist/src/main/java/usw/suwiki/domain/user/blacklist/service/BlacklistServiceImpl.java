package usw.suwiki.domain.user.blacklist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.core.secure.Encoder;
import usw.suwiki.domain.user.blacklist.BlacklistDomain;
import usw.suwiki.domain.user.blacklist.BlacklistRepository;
import usw.suwiki.domain.user.dto.UserResponse;
import usw.suwiki.domain.user.service.BlacklistService;

import java.util.Collections;
import java.util.List;

import static usw.suwiki.core.exception.ExceptionCode.BLACKLIST;

@Service
@Transactional
@RequiredArgsConstructor
class BlacklistServiceImpl implements BlacklistService {
  private final BlacklistRepository blacklistRepository;

  private final Encoder encoder;

  @Override // todo: 네이밍은 all 인데 하나만 조회한다..?
  public List<UserResponse.BlackedReason> loadAllBlacklistLogs(Long userId) {
    return blacklistRepository.findByUserIdx(userId)
      .map(this::convert)
      .orElse(Collections.emptyList());
  }

  private List<UserResponse.BlackedReason> convert(BlacklistDomain blacklistDomain) {
    return List.of(new UserResponse.BlackedReason(
      blacklistDomain.getReason(),
      blacklistDomain.getJudgement(),
      blacklistDomain.getCreateDate(),
      blacklistDomain.getExpiredAt()
    ));
  }

  @Override
  public void validateNotBlack(String email) {
    for (BlacklistDomain blackListUser : blacklistRepository.findAll()) {
      if (encoder.matches(email, blackListUser.getHashedEmail())) {
        throw new AccountException(BLACKLIST);
      }
    }
  }

  @Override
  public void black(Long userId, String email, String reason, String judgement) {
    var blacklist = BlacklistDomain.permanent(userId, encoder.encode(email), reason, judgement);
    blacklistRepository.save(blacklist);
  }

  @Override
  public void overRestricted(Long userId, String email) {
    var blacklist = BlacklistDomain.overRestrict(userId, encoder.encode(email));
    blacklistRepository.save(blacklist);
  }
}

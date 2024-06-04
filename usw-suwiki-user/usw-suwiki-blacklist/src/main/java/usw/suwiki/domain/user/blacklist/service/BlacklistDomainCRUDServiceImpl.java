package usw.suwiki.domain.user.blacklist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.core.secure.Encoder;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.blacklist.BlacklistDomain;
import usw.suwiki.domain.user.blacklist.BlacklistRepository;
import usw.suwiki.domain.user.dto.UserResponse;
import usw.suwiki.domain.user.service.BlacklistDomainCRUDService;
import usw.suwiki.domain.user.service.UserCRUDService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static usw.suwiki.domain.user.dto.UserResponse.BlackedReason;

@Service
@Transactional
@RequiredArgsConstructor
class BlacklistDomainCRUDServiceImpl implements BlacklistDomainCRUDService {
  private static final Integer NESTED_RESTRICTED_TIME = 3;
  private static final Long BANNED_PERIOD = 365L;

  private final BlacklistRepository blacklistRepository;
  private final UserCRUDService userCRUDService;
  private final Encoder encoder;

  @Override // todo: 네이밍은 all 인데 하나만 조회한다..?
  public List<BlackedReason> loadAllBlacklistLogs(Long userId) {
    return blacklistRepository.findByUserIdx(userId)
      .map(this::convert)
      .orElse(Collections.emptyList());
  }

  private List<UserResponse.BlackedReason> convert(BlacklistDomain blacklistDomain) {
    return List.of(BlackedReason.builder()
      .blackListReason(blacklistDomain.getBannedReason())
      .judgement(blacklistDomain.getJudgement())
      .createdAt(blacklistDomain.getCreateDate())
      .expiredAt(blacklistDomain.getExpiredAt())
      .build());
  }

  @Override
  public void saveBlackListDomain(Long userIdx, Long bannedPeriod, String bannedReason, String judgement) {
    User user = userCRUDService.loadUserById(userIdx);
    user.restrict();

    String hashTargetEmail = encoder.encode(user.getEmail());
    if (user.getRestrictedCount() >= NESTED_RESTRICTED_TIME) {
      bannedPeriod += BANNED_PERIOD;
    }

    BlacklistDomain blacklistDomain = BlacklistDomain.builder()
      .userIdx(user.getId())
      .bannedReason(bannedReason)
      .hashedEmail(hashTargetEmail)
      .judgement(judgement)
      .expiredAt(LocalDateTime.now().plusDays(bannedPeriod))
      .build();

    blacklistRepository.save(blacklistDomain);
  }
}

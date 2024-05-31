package usw.suwiki.domain.user.blacklist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.core.secure.PasswordEncoder;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.blacklist.BlacklistDomain;
import usw.suwiki.domain.user.blacklist.BlacklistRepository;
import usw.suwiki.domain.user.service.BlacklistDomainCRUDService;
import usw.suwiki.domain.user.service.UserCRUDService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static usw.suwiki.domain.user.dto.UserResponse.BlackedReason;

@Service
@Transactional
@RequiredArgsConstructor
class BlacklistDomainCRUDServiceImpl implements BlacklistDomainCRUDService {
  private static final Integer NESTED_RESTRICTED_TIME = 3;
  private static final Long BANNED_PERIOD = 365L;

  private final BlacklistRepository blacklistRepository;
  private final UserCRUDService userCRUDService;
  private final PasswordEncoder passwordEncoder;

  @Override
  public List<BlackedReason> loadAllBlacklistLog(Long userIdx) {
    Optional<BlacklistDomain> loadedDomain = blacklistRepository.findByUserIdx(userIdx);
    List<BlackedReason> finalResultForm = new ArrayList<>();
    if (loadedDomain.isPresent()) {
      BlackedReason blackedReason =
        BlackedReason.builder()
          .blackListReason(loadedDomain.get().getBannedReason())
          .judgement(loadedDomain.get().getJudgement())
          .createdAt(loadedDomain.get().getCreateDate())
          .expiredAt(loadedDomain.get().getExpiredAt())
          .build();
      finalResultForm.add(blackedReason);
    }
    return finalResultForm;
  }

  @Override
  public void saveBlackListDomain(Long userIdx, Long bannedPeriod, String bannedReason, String judgement) {
    User user = userCRUDService.loadUserById(userIdx);
    user.restrict();

    String hashTargetEmail = passwordEncoder.encode(user.getEmail());
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

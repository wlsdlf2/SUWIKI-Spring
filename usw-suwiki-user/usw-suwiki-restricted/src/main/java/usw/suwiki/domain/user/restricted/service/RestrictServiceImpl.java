package usw.suwiki.domain.user.restricted.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.domain.user.dto.UserResponse;
import usw.suwiki.domain.user.restricted.RestrictingUser;
import usw.suwiki.domain.user.restricted.RestrictingUserRepository;
import usw.suwiki.domain.user.service.BlacklistService;
import usw.suwiki.domain.user.service.RestrictService;
import usw.suwiki.domain.user.service.UserCRUDService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
class RestrictServiceImpl implements RestrictService {
  private final UserCRUDService userCRUDService;
  private final BlacklistService blacklistService;

  private final RestrictingUserRepository restrictingUserRepository;

  @Transactional(readOnly = true)
  @Override // todo: 네이밍은 all 인데 하나만 조회한다..?
  public List<UserResponse.RestrictedReason> loadRestrictedLog(Long userId) {
    Optional<RestrictingUser> wrappedRestrictingUser = restrictingUserRepository.findByUserIdx(userId);
    List<UserResponse.RestrictedReason> finalResultForm = new ArrayList<>();

    if (wrappedRestrictingUser.isPresent()) {
      RestrictingUser RestrictingUser = wrappedRestrictingUser.get();
      UserResponse.RestrictedReason resultForm = UserResponse.RestrictedReason
        .builder()
        .restrictedReason(RestrictingUser.getReason())
        .judgement(RestrictingUser.getJudgement())
        .createdAt(RestrictingUser.getCreateDate())
        .restrictingDate(RestrictingUser.getRestrictingDate())
        .build();
      finalResultForm.add(resultForm);
    }

    return finalResultForm;
  }

  @Transactional(readOnly = true)
  @Override
  public List<Long> loadAllRestrictedUntilNow() {
    return restrictingUserRepository.findByRestrictingDateBefore(LocalDateTime.now()).stream()
      .map(RestrictingUser::getUserIdx)
      .toList();
  }

  @Override
  public void release(Long userId) {
    restrictingUserRepository.deleteByUserIdx(userId);
  }

  @Override
  public void restrict(Long reportedId, long banPeriod, String reason, String judgement) {
    var user = userCRUDService.loadUserById(reportedId);
    user.reported();

    if (user.isArrestable()) {
      blacklistService.overRestricted(user.getId(), user.getEmail());
      return;
    }

    restrictingUserRepository.save(RestrictingUser.of(reportedId, banPeriod, reason, judgement));
  }
}

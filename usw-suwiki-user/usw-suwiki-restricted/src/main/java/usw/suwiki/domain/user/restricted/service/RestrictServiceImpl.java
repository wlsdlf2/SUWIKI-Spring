package usw.suwiki.domain.user.restricted.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.domain.user.dto.UserResponse;
import usw.suwiki.domain.user.restricted.RestrictingUser;
import usw.suwiki.domain.user.restricted.RestrictingUserRepository;
import usw.suwiki.domain.user.service.RestrictService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
class RestrictServiceImpl implements RestrictService {
  private final RestrictingUserRepository restrictingUserRepository;

  @Transactional(readOnly = true)
  @Override // todo: 네이밍은 all 인데 하나만 조회한다..?
  public List<UserResponse.RestrictedReason> loadRestrictedLog(Long userId) {
    return restrictingUserRepository.findByUserIdx(userId)
      .map(it -> List.of(
        new UserResponse.RestrictedReason(
          it.getReason(),
          it.getJudgement(),
          it.getCreateDate(),
          it.getRestrictingDate()
        )))
      .orElse(Collections.emptyList());
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
    restrictingUserRepository.save(RestrictingUser.of(reportedId, banPeriod, reason, judgement));
  }
}

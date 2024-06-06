package usw.suwiki.domain.user.isolated.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.core.secure.Encoder;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.UserRepository;
import usw.suwiki.domain.user.isolated.UserIsolation;
import usw.suwiki.domain.user.isolated.UserIsolationRepository;
import usw.suwiki.domain.user.service.UserIsolationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static usw.suwiki.core.exception.ExceptionCode.USER_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
class UserIsolationServiceImpl implements UserIsolationService {
  private final UserIsolationRepository userIsolationRepository;
  private final UserRepository userRepository; // todo: (06.06) 처치 고민하기, 다른 객체로 분리 or 현상 유지

  @Override
  @Transactional(readOnly = true)
  public List<Long> loadAllIsolatedUntil(LocalDateTime target) {
    return userIsolationRepository.findByRequestedQuitDateBefore(target).stream()
      .map(UserIsolation::getUserIdx)
      .toList();
  }

  @Override
  public long countAllIsolatedUsers() {
    return userIsolationRepository.count();
  }

  @Override
  public void wakeIfSleeping(String email) {
    wake(() -> userIsolationRepository.findByEmail(email), isolation -> {});
  }

  @Override
  public void wakeIfSleeping(String loginId, String email) {
    wake(() -> userIsolationRepository.findByLoginIdAndEmail(loginId, email), isolation -> {});
  }

  @Override
  public void wakeIfSleeping(String loginId, Encoder encoder, String password) {
    wake(() -> userIsolationRepository.findByLoginId(loginId), isolation -> isolation.validateLoginable(encoder, password));
  }

  private void wake(Supplier<Optional<UserIsolation>> query, Consumer<UserIsolation> option) {
    query.get().ifPresent(isolation -> {
      option.accept(isolation);
      var user = findUserById(isolation.getUserIdx());
      user.wake(isolation.getLoginId(), isolation.getPassword(), isolation.getEmail());
      userIsolationRepository.delete(isolation);
    });
  }

  @Override
  public void isolate(Long userId) {
    if (!userIsolationRepository.existsByUserIdx(userId)) {
      return;
    }

    var user = findUserById(userId);
    user.sleep();

    userIsolationRepository.save(UserIsolation.from(user));
  }

  @Override
  public boolean isIsolatedByEmail(String email) {
    return userIsolationRepository.existsByEmail(email);
  }

  @Override
  public boolean isIsolatedByLoginId(String loginId) {
    return userIsolationRepository.existsByLoginId(loginId);
  }

  @Override
  public void deleteByUserId(Long userId) {
    userIsolationRepository.deleteByUserIdx(userId);
  }

  private User findUserById(Long userId) {
    return userRepository.findById(userId)
      .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
  }
}

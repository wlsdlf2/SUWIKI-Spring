package usw.suwiki.domain.user.isolated.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.core.exception.ExceptionCode;
import usw.suwiki.core.secure.Encoder;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.isolated.UserIsolation;
import usw.suwiki.domain.user.isolated.UserIsolationRepository;
import usw.suwiki.domain.user.service.UserIsolationService;
import usw.suwiki.domain.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class UserIsolationServiceImpl implements UserIsolationService {
  private final UserIsolationRepository userIsolationRepository;

  @Override
  public List<Long> loadAllIsolatedUntil(LocalDateTime target) {
    return userIsolationRepository.findByRequestedQuitDateBefore(target).stream()
      .map(UserIsolation::getUserIdx)
      .toList();
  }

  @Override
  public boolean isNotSleepingByUserId(Long userId) {
    return !userIsolationRepository.existsByUserIdx(userId);
  }

  @Override
  public boolean isSleeping(String loginId, String email) {
    return userIsolationRepository.existsByLoginIdAndEmail(loginId, email);
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
  public Optional<String> findIsolatedLoginIdByEmail(String email) {
    return userIsolationRepository.findByEmail(email).map(UserIsolation::getLoginId);
  }

  @Override
  @Transactional
  public String updateIsolatedUserPassword(Encoder encoder, String email) {
    return userIsolationRepository.findByEmail(email)
      .map(it -> it.updateRandomPassword(encoder))
      .orElseThrow(() -> new AccountException(ExceptionCode.USER_NOT_FOUND));
  }

  @Override
  public boolean isLoginable(String loginId, String inputPassword, Encoder encoder) {
    return userIsolationRepository.findByLoginId(loginId)
      .map(it -> it.isPasswordEquals(encoder, inputPassword))
      .orElseThrow(() -> new AccountException(ExceptionCode.USER_NOT_FOUND));
  }

  @Override
  @Transactional
  public User wake(UserService userService, String loginId) {
    var userIsolation = userIsolationRepository.findByLoginId(loginId)
      .orElseThrow(() -> new AccountException(ExceptionCode.USER_NOT_FOUND));

    User user = userService.loadUserById(userIsolation.getUserIdx());
    user.wake(userIsolation.getLoginId(), userIsolation.getPassword(), userIsolation.getEmail());

    userIsolationRepository.deleteByLoginId(loginId);
    return user;
  }

  @Override
  @Transactional
  public void saveFrom(User user) {
    userIsolationRepository.save(UserIsolation.builder()
      .userIdx(user.getId())
      .loginId(user.getLoginId())
      .password(user.getPassword())
      .email(user.getEmail())
      .lastLogin(user.getLastLogin())
      .requestedQuitDate(user.getRequestedQuitDate())
      .build());
  }

  @Override
  @Transactional
  public void deleteByUserId(Long userId) {
    userIsolationRepository.deleteByUserIdx(userId);
  }

  @Override
  public long countAllIsolatedUsers() {
    return userIsolationRepository.count();
  }
}

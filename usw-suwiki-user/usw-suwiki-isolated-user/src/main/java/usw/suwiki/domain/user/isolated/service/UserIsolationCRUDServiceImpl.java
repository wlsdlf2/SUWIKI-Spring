package usw.suwiki.domain.user.isolated.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.core.exception.ExceptionType;
import usw.suwiki.core.secure.Encoder;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.isolated.UserIsolation;
import usw.suwiki.domain.user.isolated.UserIsolationRepository;
import usw.suwiki.domain.user.service.UserCRUDService;
import usw.suwiki.domain.user.service.UserIsolationCRUDService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class UserIsolationCRUDServiceImpl implements UserIsolationCRUDService {
  private final UserIsolationRepository userIsolationRepository;

  @Override
  public List<Long> loadAllIsolatedUntilTarget(LocalDateTime target) {
    return userIsolationRepository.findByRequestedQuitDateBefore(target).stream()
      .map(UserIsolation::getUserIdx)
      .toList();
  }

  @Override
  public boolean isIsolatedByEmail(String email) {
    return userIsolationRepository.findByEmail(email).isPresent();
  }

  @Override
  public boolean isIsolatedByLoginId(String loginId) {
    return userIsolationRepository.findByLoginId(loginId).isPresent();
  }

  @Override
  public Optional<String> getIsolatedLoginIdByEmail(String email) {
    return loadWrappedUserFromEmail(email)
      .map(UserIsolation::getLoginId);
  }

  @Override
  public boolean isRetrievedUserEquals(String email, String loginId) {
    Optional<UserIsolation> byEmail = userIsolationRepository.findByEmail(email);
    Optional<UserIsolation> byLoginId = userIsolationRepository.findByLoginId(loginId);

    return byEmail.isPresent() && byLoginId.isPresent() && byEmail.equals(byLoginId);
  }

  @Override
  @Transactional
  public String updateIsolatedUserPassword(Encoder encoder, String email) {
    return userIsolationRepository.findByEmail(email)
      .map(it -> it.updateRandomPassword(encoder))
      .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_FOUND));
  }

  @Override
  public boolean isLoginable(String loginId, String inputPassword, Encoder encoder) {
    return userIsolationRepository.findByLoginId(loginId)
      .map(it -> it.validatePassword(encoder, inputPassword))
      .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_FOUND));
  }

  @Override
  @Transactional
  public User wakeIsolated(UserCRUDService userCRUDService, String loginId) {
    UserIsolation userIsolation = userIsolationRepository.findByLoginId(loginId)
      .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_FOUND));

    User user = userCRUDService.loadUserById(userIsolation.getUserIdx());
    user.wake(userIsolation.getLoginId(), userIsolation.getPassword(), userIsolation.getEmail());

    userIsolationRepository.deleteByLoginId(loginId);
    return user;
  }

  @Override
  @Transactional
  public void saveUserIsolation(User user) {
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
  public void deleteByUserIdx(Long userIdx) {
    userIsolationRepository.deleteByUserIdx(userIdx);
  }

  @Override
  public boolean isNotIsolated(Long userId) {
    return userIsolationRepository.findByUserIdx(userId).isEmpty();
  }

  @Override
  public long countAllIsolatedUsers() {
    return userIsolationRepository.count();
  }

  public Optional<UserIsolation> loadWrappedUserFromEmail(String email) {
    return userIsolationRepository.findByEmail(email);
  }
}

package usw.suwiki.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.core.exception.ExceptionType;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.UserRepository;
import usw.suwiki.domain.user.model.UserAdapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCRUDService implements UserAdapterService {
  private final UserRepository userRepository;

  public void saveUser(User user) {
    userRepository.save(user);
  }

  public List<User> loadUsersLastLoginBetween(LocalDateTime startTime, LocalDateTime endTime) {
    return userRepository.findByLastLoginBetween(startTime, endTime);
  }

  public User loadUserById(Long userId) {
    return userRepository.findById(userId)
      .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_FOUND));
  }

  public Optional<User> findOptionalByLoginId(String loginId) {
    return userRepository.findByLoginId(loginId);
  }

  public Optional<User> findOptionalByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public User loadByLoginId(String loginId) {
    return userRepository.findByLoginId(loginId)
      .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_FOUND));
  }

  public long countAllUsers() {
    return userRepository.count();
  }

  public void deleteById(Long userId) {
    userRepository.deleteById(userId);
  }

  public void sleep(Long userIdx) {
    User user = loadUserById(userIdx);
    user.sleep();
  }

  @Override
  public UserAdapter findByUsername(String username) {
    return userRepository.findByLoginId(username)
      .map(User::toAdapter)
      .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_FOUND));
  }
}

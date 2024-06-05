package usw.suwiki.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.core.exception.ExceptionType;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.UserRepository;
import usw.suwiki.domain.user.model.UserAdapter;

@Service
@Transactional
@RequiredArgsConstructor
class UserAdapterServiceImpl implements UserAdapterService {
  private final UserRepository userRepository;

  @Override
  public UserAdapter findByUsername(String username) {
    return userRepository.findByLoginId(username)
      .map(User::toAdapter)
      .orElseThrow(() -> new AccountException(ExceptionType.USER_NOT_FOUND));
  }
}

package usw.suwiki.auth.common;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.core.exception.ExceptionCode;
import usw.suwiki.domain.user.UserRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
    return userRepository.findByLoginId(loginId)
      .map(this::toDetails)
      .orElseThrow(() -> new AccountException(ExceptionCode.USER_NOT_FOUND));
  }

  private UserDetails toDetails(usw.suwiki.domain.user.User user) {
    return new User(
      String.valueOf(user.getId()),
      "",
      List.of(new SimpleGrantedAuthority(user.getRole().name()))
    );
  }
}

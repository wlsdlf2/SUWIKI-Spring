package usw.suwiki.auth.token.service;

public interface ConfirmUserService {
  void delete(Long userId);

  void activate(Long userId);
}

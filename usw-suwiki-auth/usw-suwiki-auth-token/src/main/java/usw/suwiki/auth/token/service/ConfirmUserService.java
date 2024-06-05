package usw.suwiki.auth.token.service;

/**
 * User 모듈과 Token 모듈의 Cycle 해소 및 DIP 를 위해서 존재
 *
 * @author hejow
 */
public interface ConfirmUserService {
  void delete(Long userId);

  void activate(Long userId);
}

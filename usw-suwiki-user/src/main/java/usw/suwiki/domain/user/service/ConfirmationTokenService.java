package usw.suwiki.domain.user.service;

public interface ConfirmationTokenService {
  void validateEmailAuthorized(Long userId);

  String requestConfirm(Long userId);

  String confirm(String token);

  void deleteByUserId(Long userIdx);
}

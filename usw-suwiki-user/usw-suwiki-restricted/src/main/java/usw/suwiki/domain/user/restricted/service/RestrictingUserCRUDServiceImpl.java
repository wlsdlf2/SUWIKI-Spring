package usw.suwiki.domain.user.restricted.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.domain.user.restricted.RestrictingUser;
import usw.suwiki.domain.user.restricted.RestrictingUserRepository;
import usw.suwiki.domain.user.service.RestrictingUserCRUDService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static usw.suwiki.domain.user.dto.UserResponse.RestrictedReason;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class RestrictingUserCRUDServiceImpl implements RestrictingUserCRUDService {

  private final RestrictingUserRepository restrictingUserRepository;

  @Override
  public List<RestrictedReason> loadRestrictedLog(Long userIdx) {
    Optional<RestrictingUser> wrappedRestrictingUser = restrictingUserRepository.findByUserIdx(userIdx);
    List<RestrictedReason> finalResultForm = new ArrayList<>();

    if (wrappedRestrictingUser.isPresent()) {
      RestrictingUser RestrictingUser = wrappedRestrictingUser.get();
      RestrictedReason resultForm = RestrictedReason
        .builder()
        .restrictedReason(RestrictingUser.getRestrictingReason())
        .judgement(RestrictingUser.getJudgement())
        .createdAt(RestrictingUser.getCreateDate())
        .restrictingDate(RestrictingUser.getRestrictingDate())
        .build();
      finalResultForm.add(resultForm);
    }

    return finalResultForm;
  }
}

package usw.suwiki.domain.favoritemajor.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import usw.suwiki.domain.favoritemajor.FavoriteMajor;
import usw.suwiki.domain.favoritemajor.dto.FavoriteSaveDto;
import usw.suwiki.domain.favoritemajor.repository.FavoriteMajorRepositoryV2;
import usw.suwiki.domain.user.user.User;
import usw.suwiki.domain.user.user.service.UserBusinessService;
import usw.suwiki.domain.user.user.service.UserCRUDService;
import usw.suwiki.global.exception.errortype.FavoriteMajorException;
import usw.suwiki.global.jwt.JwtAgent;

import javax.transaction.Transactional;
import java.util.List;

import static usw.suwiki.global.exception.ExceptionType.*;

@Transactional
@RequiredArgsConstructor
@Service
public class FavoriteMajorServiceV2 {
    private final UserCRUDService userCRUDService;
    private final UserBusinessService userBusinessService;
    private final FavoriteMajorRepositoryV2 favoriteMajorRepositoryV2;
    private final JwtAgent jwtAgent;

    public void save(String authorization, FavoriteSaveDto favoriteSaveDto) {
        userBusinessService.validateRestrictedUser(authorization);
        User loginUser = userCRUDService.loadUserByIdx(jwtAgent.getId(authorization));

        String majorType = favoriteSaveDto.getMajorType();
        validateDuplicateFavoriteMajor(loginUser, majorType);
        FavoriteMajor favoriteMajor = FavoriteMajor.builder()
                .user(loginUser)
                .majorType(majorType)
                .build();
        favoriteMajorRepositoryV2.save(favoriteMajor);
    }

    private void validateDuplicateFavoriteMajor(User loginUser, String majorType) {
        boolean exists = favoriteMajorRepositoryV2.existsByUserIdAndMajorType(loginUser.getId(), majorType);
        if (!exists) return;

        throw new FavoriteMajorException(FAVORITE_MAJOR_DUPLICATE_REQUEST);
    }

    public List<String> findAllMajorTypeByUser(String authorization) {
        userBusinessService.validateRestrictedUser(authorization);
        User loginUser = userCRUDService.loadUserByIdx(jwtAgent.getId(authorization));

        List<FavoriteMajor> favoriteMajors = favoriteMajorRepositoryV2.findAllByUserId(loginUser.getId());
        return favoriteMajors.stream().map(FavoriteMajor::getMajorType).toList();
    }

    public void delete(String authorization,  String majorType) {
        userBusinessService.validateRestrictedUser(authorization);
        User loginUser = userCRUDService.loadUserByIdx(jwtAgent.getId(authorization));

        FavoriteMajor favoriteMajor = favoriteMajorRepositoryV2.findByUserIdAndMajorType(loginUser.getId(), majorType)
                .orElseThrow(() -> new FavoriteMajorException(FAVORITE_MAJOR_NOT_FOUND));

        favoriteMajorRepositoryV2.delete(favoriteMajor);
    }

    public void deleteAllFromUserIdx(Long userIdx) {
        User loginUser = userCRUDService.loadUserByIdx(userIdx);
        List<FavoriteMajor> favoriteMajors = favoriteMajorRepositoryV2.findAllByUserId(loginUser.getId());
        favoriteMajorRepositoryV2.deleteAll(favoriteMajors);
    }
}

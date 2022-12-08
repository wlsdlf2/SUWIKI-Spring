package usw.suwiki.domain.restrictinguser.service.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.domain.admin.dto.UserAdminRequestDto.EvaluatePostRestrictForm;
import usw.suwiki.domain.admin.dto.UserAdminRequestDto.ExamPostRestrictForm;
import usw.suwiki.domain.admin.service.UserAdminService;
import usw.suwiki.domain.evaluation.entity.EvaluatePosts;
import usw.suwiki.domain.exam.entity.ExamPosts;
import usw.suwiki.domain.restrictinguser.repository.RestrictingUser;
import usw.suwiki.domain.user.entity.User;
import usw.suwiki.domain.user.repository.restrictinguser.RestrictingUserRepository;
import usw.suwiki.domain.user.service.UserService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class RestrictingUserAddRestrictingUserUseCase {

    private final UserService userService;
    private final UserAdminService userAdminService;
    private final RestrictingUserRepository restrictingUserRepository;

    private void commonMethod(
            EvaluatePostRestrictForm evaluatePostRestrictForm, ExamPostRestrictForm examPostRestrictForm) {

        if (evaluatePostRestrictForm != null) {
            EvaluatePosts evaluatePosts = userService.loadEvaluatePostsByIndex(evaluatePostRestrictForm.getEvaluateIdx());
            User targetUser = userService.loadUserFromUserIdx(evaluatePosts.getUser().getId());
            if (targetUser.getRestrictedCount() >= 2) {
                userAdminService.blacklistOrRestrictAndDeleteExamPost(evaluatePosts.getId());
                userAdminService.executeBlacklistByEvaluatePost(
                        targetUser.getId(), 90L,
                        "신고 누적으로 인한 블랙리스트", "신고누적 블랙리스트 1년");
            } else if (targetUser.getRestrictedCount() < 3) {
                targetUser.editRestricted(true);
                RestrictingUser restrictingUser = RestrictingUser.builder()
                        .userIdx(targetUser.getId())
                        .restrictingDate(LocalDateTime.now().plusDays(evaluatePostRestrictForm.getRestrictingDate()))
                        .restrictingReason(evaluatePostRestrictForm.getRestrictingReason())
                        .judgement(evaluatePostRestrictForm.getJudgement())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                restrictingUserRepository.save(restrictingUser);
                return;
            }
        }

        ExamPosts examPosts = userService.loadExamPostsByIndex(examPostRestrictForm.getExamIdx());
        User user = userService.loadUserFromUserIdx(examPosts.getUser().getId());

        if (user.getRestrictedCount() >= 2) {
            userAdminService.blacklistOrRestrictAndDeleteExamPost(examPosts.getId());
            userAdminService.executeBlacklistByExamPost(
                    user.getId(), 90L,
                    "신고 누적으로 인한 블랙리스트", "신고누적 블랙리스트 1년");
        } else if (user.getRestrictedCount() < 3) {
            user.editRestricted(true);
            RestrictingUser restrictingUser = RestrictingUser.builder()
                    .userIdx(user.getId())
                    .restrictingDate(LocalDateTime.now().plusDays(examPostRestrictForm.getRestrictingDate()))
                    .restrictingReason(examPostRestrictForm.getRestrictingReason())
                    .judgement(examPostRestrictForm.getJudgement())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now()).build();
            restrictingUserRepository.save(restrictingUser);
        }
    }

    // 강의평가 게시글로 유저 정지 먹이기
    @Transactional
    public void executeEvaluatePost(EvaluatePostRestrictForm evaluatePostRestrictForm) {
        commonMethod(evaluatePostRestrictForm, null);
    }

    // 시험정보 게시글로 유저 정지 먹이기
    @Transactional
    public void executeExamPost(ExamPostRestrictForm examPostRestrictForm) {
        commonMethod(null, examPostRestrictForm);
    }
}

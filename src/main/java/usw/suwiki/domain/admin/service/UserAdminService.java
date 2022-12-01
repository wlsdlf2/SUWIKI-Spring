package usw.suwiki.domain.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.domain.blacklistdomain.BlacklistRepository;
import usw.suwiki.domain.blacklistdomain.entity.BlacklistDomain;
import usw.suwiki.domain.evaluation.entity.EvaluatePosts;
import usw.suwiki.domain.evaluation.service.EvaluatePostsService;
import usw.suwiki.domain.exam.entity.ExamPosts;
import usw.suwiki.domain.exam.service.ExamPostsService;
import usw.suwiki.domain.postreport.entity.EvaluatePostReport;
import usw.suwiki.domain.postreport.entity.ExamPostReport;
import usw.suwiki.domain.postreport.repository.EvaluateReportRepository;
import usw.suwiki.domain.postreport.repository.ExamReportRepository;
import usw.suwiki.domain.user.entity.User;
import usw.suwiki.domain.user.service.UserService;
import usw.suwiki.global.exception.errortype.AccountException;

import java.time.LocalDateTime;
import java.util.List;

import static usw.suwiki.global.exception.ErrorType.SERVER_ERROR;
import static usw.suwiki.global.exception.ErrorType.USER_ALREADY_BLACKLISTED;

@Service
@Transactional
@RequiredArgsConstructor
public class UserAdminService {
    private final UserService userService;
    private final BlacklistRepository blacklistRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EvaluatePostsService evaluatePostsService;
    private final ExamPostsService examPostsService;
    private final EvaluateReportRepository evaluateReportRepository;
    private final ExamReportRepository examReportRepository;

    public void banUserByEvaluate(Long userIdx, Long bannedPeriod, String bannedReason, String judgement) {
        User user = userService.loadUserFromUserIdx(userIdx);
        user.setRestricted(true);

        String hashTargetEmail = bCryptPasswordEncoder.encode(user.getEmail());
        if (blacklistRepository.findByUserId(user.getId()).isPresent()) {
            throw new AccountException(USER_ALREADY_BLACKLISTED);
        }

        if (user.getRestrictedCount() >= 3) {
            bannedPeriod += 365L;
        }

        BlacklistDomain blacklistDomain = BlacklistDomain.builder()
                .userIdx(user.getId())
                .bannedReason(bannedReason)
                .hashedEmail(hashTargetEmail)
                .judgement(judgement)
                .expiredAt(LocalDateTime.now().plusDays(bannedPeriod))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        blacklistRepository.save(blacklistDomain);
    }

    // 시험정보 블랙리스트
    public void banUserByExam(Long userIdx, Long bannedPeriod, String bannedReason, String judgement) {
        User user = userService.loadUserFromUserIdx(userIdx);
        user.setRestricted(true);

        String hashTargetEmail = bCryptPasswordEncoder.encode(user.getEmail());

        if (blacklistRepository.findByUserId(user.getId()).isPresent()) {
            throw new AccountException(USER_ALREADY_BLACKLISTED);
        }

        if (user.getRestrictedCount() >= 3) {
            bannedPeriod += 365L;
        }

        BlacklistDomain blacklistDomain = BlacklistDomain.builder()
                .userIdx(user.getId())
                .bannedReason(bannedReason)
                .judgement(judgement)
                .hashedEmail(hashTargetEmail)
                .expiredAt(LocalDateTime.now().plusDays(bannedPeriod))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        blacklistRepository.save(blacklistDomain);
    }

    public Long banishEvaluatePost(Long evaluateIdx) {
        if (userService.loadEvaluatePostsByIndex(evaluateIdx) != null) {
            EvaluatePosts targetedEvaluatePost = userService.loadEvaluatePostsByIndex(evaluateIdx);
            Long targetedEvaluatePostIdx = targetedEvaluatePost.getId();
            Long targetedUserIdx = targetedEvaluatePost.getUser().getId();
            evaluateReportRepository.deleteByEvaluateIdx(targetedEvaluatePostIdx);
            evaluatePostsService.deleteById(targetedEvaluatePostIdx, targetedUserIdx);
            return targetedUserIdx;
        }
        throw new AccountException(SERVER_ERROR);
    }

    public Long blacklistOrRestrictAndDeleteExamPost(Long examIdx) {
        if (userService.loadExamPostsByIndex(examIdx) != null) {
            ExamPosts targetedExamPost = userService.loadExamPostsByIndex(examIdx);
            Long targetedExamPostIdx = targetedExamPost.getId();
            Long targetedUserIdx = targetedExamPost.getUser().getId();
            examReportRepository.deleteByExamIdx(targetedExamPostIdx);
            examPostsService.deleteById(targetedExamPostIdx, targetedUserIdx);
            return targetedUserIdx;
        }
        throw new AccountException(SERVER_ERROR);
    }

    public User plusRestrictCount(Long userIdx) {
        User user = userService.loadUserFromUserIdx(userIdx);
        user.setRestrictedCount(user.getRestrictedCount() + 1);
        return user;
    }

    public User plusReportingUserPoint(Long reportingUserIdx) {
        User user = userService.loadUserFromUserIdx(reportingUserIdx);
        user.setPoint(user.getPoint() + 1);
        return user;
    }

    public List<EvaluatePostReport> getReportedEvaluateList() {
        return evaluateReportRepository.loadAllReportedPosts();
    }

    public List<ExamPostReport> getReportedExamList() {
        return examReportRepository.loadAllReportedPosts();
    }
}

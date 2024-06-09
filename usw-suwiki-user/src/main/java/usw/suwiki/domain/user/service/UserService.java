package usw.suwiki.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.core.exception.AccountException;
import usw.suwiki.core.secure.Encoder;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.UserRepository;
import usw.suwiki.domain.user.dto.UserResponse;

import java.time.LocalDateTime;
import java.util.List;

import static usw.suwiki.core.exception.ExceptionCode.USER_NOT_FOUND;
import static usw.suwiki.domain.user.dto.UserResponse.BlackedReason;
import static usw.suwiki.domain.user.dto.UserResponse.MyPage;
import static usw.suwiki.domain.user.dto.UserResponse.RestrictedReason;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final BlacklistService blacklistService;
  private final UserIsolationService userIsolationService;

  private final RestrictService restrictService;
  private final FavoriteMajorService favoriteMajorService;

  private final Encoder encoder;

  @Transactional(readOnly = true)
  public List<User> loadUsersLastLoginBetween(LocalDateTime startTime, LocalDateTime endTime) {
    return userRepository.findByLastLoginBetween(startTime, endTime);
  }

  @Transactional(readOnly = true)
  public List<String> loadAllFavoriteMajors(Long userId) {
    return favoriteMajorService.findMajorTypeByUser(userId);
  }

  @Transactional(readOnly = true)
  public UserResponse.MyPage loadMyPage(Long userId) {
    User user = loadById(userId);
    return new MyPage(user.getLoginId(), user.getEmail(), user.getPoint(), user.getWrittenEvaluation(), user.getWrittenExam(), user.getViewExamCount());
  }

  @Transactional(readOnly = true)
  public List<BlackedReason> loadBlackListReason(Long id) {
    var user = loadById(id);
    return blacklistService.loadAllBlacklistLogs(user.getId());
  }

  @Transactional(readOnly = true)
  public List<RestrictedReason> loadRestrictedReason(Long userId) {
    var user = loadById(userId);
    return restrictService.loadRestrictedLog(user.getId());
  }

  public long countAllUsers() {
    return userRepository.count() + userIsolationService.countAllIsolatedUsers();
  }

  public void rewardReport(Long id) {
    var user = loadById(id);
    user.rewardReport();
  }

  public void evaluate(Long userId) {
    var user = loadById(userId);
    user.evaluate();
  }

  public void eraseEvaluation(Long userId) {
    var user = loadById(userId);
    user.eraseEvaluation();
  }

  public void writeExamPost(Long userId) {
    var user = loadById(userId);
    user.writeExamPost();
  }

  public void purchaseExamPost(Long userId) {
    var user = loadById(userId);
    user.purchaseExamPost();
  }

  public void eraseExamPost(Long userId) {
    var user = loadById(userId);
    user.eraseExamPost();
  }

  public void changePassword(Long userId, String prePassword, String newPassword) {
    var user = loadById(userId);
    user.changePassword(encoder, prePassword, newPassword);
  }

  public void activate(Long userId) {
    userRepository.findById(userId)
      .orElseThrow(() -> new AccountException(USER_NOT_FOUND))
      .activate();
  }

  public void quit(Long userId, String inputPassword) {
    var user = loadById(userId);
    user.validatePassword(encoder, inputPassword);
    user.waitQuit();
  }

  public void restrict(Long reportedId, long banPeriod, String reason, String judgement) {
    var user = loadById(reportedId);
    user.reported();

    if (user.isArrestable()) {
      blacklistService.overRestricted(user.getId(), user.getEmail());
      return;
    }

    restrictService.restrict(reportedId, banPeriod, reason, judgement);
  }

  public void release(Long reportedId) {
    var user = loadById(reportedId);
    user.release();
    restrictService.release(reportedId);
  }

  public void black(Long reportedId, String reason, String judgement) {
    var user = loadById(reportedId);
    user.reported();

    blacklistService.black(reportedId, user.getEmail(), reason, judgement);
  }

  public void saveFavoriteMajor(Long userId, String majorType) {
    favoriteMajorService.save(userId, majorType);
  }

  public void deleteFavoriteMajor(Long userId, String majorType) {
    favoriteMajorService.delete(userId, majorType);
  }

  public void deleteById(Long userId) {
    userRepository.deleteById(userId);
  }

  public void deleteAllInBatch(List<Long> userIds) {
    userRepository.deleteAllByIdInBatch(userIds);
  }

  public User loadById(Long userId) {
    return userRepository.findById(userId)
      .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
  }
}

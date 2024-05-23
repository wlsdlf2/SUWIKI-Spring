package usw.suwiki.test.fixture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import usw.suwiki.auth.token.ConfirmationToken;
import usw.suwiki.auth.token.ConfirmationTokenRepository;
import usw.suwiki.auth.token.RefreshTokenRepository;
import usw.suwiki.core.secure.TokenAgent;
import usw.suwiki.domain.evaluatepost.EvaluatePost;
import usw.suwiki.domain.evaluatepost.EvaluatePostRepository;
import usw.suwiki.domain.exampost.ExamPost;
import usw.suwiki.domain.exampost.ExamPostRepository;
import usw.suwiki.domain.lecture.Lecture;
import usw.suwiki.domain.lecture.LectureRepository;
import usw.suwiki.domain.lecture.major.FavoriteMajorRepository;
import usw.suwiki.domain.lecture.schedule.LectureSchedule;
import usw.suwiki.domain.lecture.schedule.LectureScheduleRepository;
import usw.suwiki.domain.lecture.timetable.Timetable;
import usw.suwiki.domain.lecture.timetable.TimetableCell;
import usw.suwiki.domain.lecture.timetable.TimetableRepository;
import usw.suwiki.domain.notice.Notice;
import usw.suwiki.domain.notice.NoticeRepository;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.UserRepository;
import usw.suwiki.domain.user.blacklist.BlacklistRepository;
import usw.suwiki.domain.user.isolated.UserIsolationRepository;
import usw.suwiki.domain.user.model.UserClaim;
import usw.suwiki.domain.user.restricted.RestrictingUserRepository;
import usw.suwiki.domain.viewexam.ViewExam;
import usw.suwiki.domain.viewexam.ViewExamRepository;

import java.util.List;
import java.util.stream.IntStream;

@Component
public final class Fixtures {
  @Autowired
  private ConfirmationTokenRepository confirmationTokenRepository;
  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  private EvaluatePostRepository evaluatePostRepository;
  @Autowired
  private ExamPostRepository examPostRepository;
  @Autowired
  private NoticeRepository noticeRepository;
  @Autowired
  private ViewExamRepository viewExamRepository;

  @Autowired
  private LectureRepository lectureRepository;
  @Autowired
  private LectureScheduleRepository lectureScheduleRepository;
  @Autowired
  private TimetableRepository timetableRepository;
  @Autowired
  private FavoriteMajorRepository favoriteMajorRepository;

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private BlacklistRepository blacklistRepository;
  @Autowired
  private UserIsolationRepository userIsolationRepository;
  @Autowired
  private RestrictingUserRepository restrictingUserRepository;

  @Autowired
  private TokenAgent tokenAgent;

  public String 토큰_생성() {
    var user = 유저_생성();
    return 토큰_생성(user);
  }

  public String 토큰_생성(User user) {
    return tokenAgent.createAccessToken(user.getId(), user.toClaim());
  }

  public String 제한된_사용자_토큰_생성() {
    return tokenAgent.createAccessToken(-1L, new UserClaim("loginId", "USER", true));
  }

  public String 다른_사용자_토큰_생성() {
    var another = 다른_유저_생성();
    return tokenAgent.createAccessToken(another.getId(), another.toClaim());
  }

  public User 관리자_생성() {
    return userRepository.save(UserFixture.admin());
  }

  public User 유저_생성() {
    return userRepository.save(UserFixture.one());
  }

  public User 다른_유저_생성() {
    return userRepository.save(UserFixture.another());
  }

  public Timetable 시간표_생성(Long userId) {
    return timetableRepository.save(TimetableFixture.one(userId));
  }

  public Timetable 다른_시간표_생성(Long userId) {
    return timetableRepository.save(TimetableFixture.another(userId));
  }

  public TimetableCell 시간표_셀_생성(String day, int start, int end) {
    return TimetableFixture.cell(day, start, end);
  }

  public Lecture 강의_생성() {
    return lectureRepository.save(LectureFixture.one());
  }

  public List<Lecture> 강의_여러개_생성(int size) {
    return lectureRepository.saveAll(LectureFixture.many(size));
  }

  public LectureSchedule 강의_일정_생성(Long lectureId) {
    return lectureScheduleRepository.save(LectureFixture.schedule(lectureId));
  }

  public Notice 공지사항_생성() {
    return noticeRepository.save(new Notice("제목", "내용"));
  }

  public List<Notice> 공지사항_여러개_생성(int size) {
    return noticeRepository.saveAll(IntStream.range(0, size)
      .mapToObj(it -> new Notice("제목" + it, "내용" + it))
      .toList());
  }

  public ExamPost 시험평가_생성(Long userId, Lecture lecture) {
    return examPostRepository.save(ExamPostFixture.one(userId, lecture));
  }

  public List<ExamPost> 시험평가_여러개_생성(Long userId, Lecture lecture, int size) {
    return examPostRepository.saveAll(ExamPostFixture.many(userId, lecture, size));
  }

  public List<ExamPost> 시험평가_여러개_생성_유저_제외(Long userId, Lecture lecture, int size) {
    return examPostRepository.saveAll(ExamPostFixture.manyWithoutUser(userId, lecture, size));
  }

  public EvaluatePost 강의평가_생성(Long userId, Lecture lecture) {
    return evaluatePostRepository.save(EvaluatePostFixture.one(userId, lecture));
  }

  public List<EvaluatePost> 강의평가_여러개_생성(Long userId, Lecture lecture, int size) {
    return evaluatePostRepository.saveAll(EvaluatePostFixture.many(userId, lecture, size));
  }

  public List<EvaluatePost> 강의평가_여러개_생성_유저_제외(Long userId, Lecture lecture, int size) {
    return evaluatePostRepository.saveAll(EvaluatePostFixture.manyWithoutUser(userId, lecture, size));
  }

  public ViewExam 시험평가_구매이력_생성(Long userId, Long lectureId) {
    return viewExamRepository.save(new ViewExam(userId, lectureId));
  }

  public void 포인트_충전(Long userId) {
    var user = userRepository.findById(userId).orElseThrow();

    for (int i = 0; i < 10; i++) {
      user.writeEvaluatePost();
      user.writeExamPost();
    }

    userRepository.save(user);
  }

  public ConfirmationToken 가입_인증_토큰_생성(Long userId) {
    return confirmationTokenRepository.save(new ConfirmationToken(userId));
  }
}

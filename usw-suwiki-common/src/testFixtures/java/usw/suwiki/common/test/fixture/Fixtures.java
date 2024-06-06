package usw.suwiki.common.test.fixture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import usw.suwiki.auth.token.ConfirmationToken;
import usw.suwiki.auth.token.ConfirmationTokenRepository;
import usw.suwiki.core.secure.TokenAgent;
import usw.suwiki.domain.evaluatepost.EvaluatePost;
import usw.suwiki.domain.evaluatepost.EvaluatePostRepository;
import usw.suwiki.domain.exampost.ExamPost;
import usw.suwiki.domain.exampost.ExamPostRepository;
import usw.suwiki.domain.lecture.Lecture;
import usw.suwiki.domain.lecture.LectureRepository;
import usw.suwiki.domain.lecture.Major;
import usw.suwiki.domain.lecture.major.FavoriteMajor;
import usw.suwiki.domain.lecture.major.FavoriteMajorRepository;
import usw.suwiki.domain.lecture.schedule.LectureSchedule;
import usw.suwiki.domain.lecture.schedule.LectureScheduleRepository;
import usw.suwiki.domain.lecture.timetable.Timetable;
import usw.suwiki.domain.lecture.timetable.TimetableCell;
import usw.suwiki.domain.lecture.timetable.TimetableRepository;
import usw.suwiki.domain.notice.Notice;
import usw.suwiki.domain.notice.NoticeRepository;
import usw.suwiki.domain.report.EvaluatePostReport;
import usw.suwiki.domain.report.EvaluateReportRepository;
import usw.suwiki.domain.report.ExamPostReport;
import usw.suwiki.domain.report.ExamReportRepository;
import usw.suwiki.domain.user.User;
import usw.suwiki.domain.user.UserRepository;
import usw.suwiki.domain.user.blacklist.BlacklistDomain;
import usw.suwiki.domain.user.blacklist.BlacklistRepository;
import usw.suwiki.domain.user.isolated.UserIsolation;
import usw.suwiki.domain.user.isolated.UserIsolationRepository;
import usw.suwiki.domain.user.model.UserClaim;
import usw.suwiki.domain.user.restricted.RestrictingUser;
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
  private EvaluateReportRepository evaluateReportRepository;
  @Autowired
  private ExamReportRepository examReportRepository;

  @Autowired
  private TokenAgent tokenAgent;

  // 토큰

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

  public String 리프레시_토큰_생성(Long userId) {
    return tokenAgent.login(userId);
  }

  public ConfirmationToken 가입_인증_토큰_생성(Long userId) {
    return confirmationTokenRepository.save(new ConfirmationToken(userId));
  }

  public ConfirmationToken 가입_인증된_토큰_생성(Long userId) {
    var token = new ConfirmationToken(userId);
    token.confirm();

    return confirmationTokenRepository.save(token);
  }

  // 사용자

  public User 유저_생성() {
    return userRepository.save(UserFixture.one(null, null));
  }

  public User 유저_생성(String loginId, String password) {
    return userRepository.save(UserFixture.one(loginId, password));
  }

  public User 다른_유저_생성() {
    return userRepository.save(UserFixture.another());
  }

  public User 관리자_생성() {
    return userRepository.save(UserFixture.admin(null, null));
  }

  public User 관리자_생성(String loginId, String password) {
    return userRepository.save(UserFixture.admin(loginId, password));
  }

  public void 포인트_충전(Long userId) {
    var user = userRepository.findById(userId).orElseThrow();

    for (int i = 0; i < 10; i++) {
      user.evaluate();
      user.writeExamPost();
    }

    userRepository.save(user);
  }

  public BlacklistDomain 블랙_리스트_생성(Long userId) {
    var user = userRepository.findById(userId).orElseThrow();
    var blacklist = BlacklistDomain.overRestrict(userId, user.getEmail());

    user.restrict();

    userRepository.save(user);
    return blacklistRepository.save(blacklist);
  }

  public RestrictingUser 이용제한_내역_생성(Long userId) {
    var user = userRepository.findById(userId).orElseThrow();
    var restricted = RestrictingUser.of(userId, 100L, "그냥", "사형");

    user.restrict();

    userRepository.save(user);
    return restrictingUserRepository.save(restricted);
  }

  public void 휴면_전환(User user) {
    userIsolationRepository.save(UserIsolation.from(user));

    user.sleep();
    userRepository.save(user);
  }

  // 시간표

  public Timetable 시간표_생성(Long userId) {
    return timetableRepository.save(TimetableFixture.one(userId));
  }

  public Timetable 다른_시간표_생성(Long userId) {
    return timetableRepository.save(TimetableFixture.another(userId));
  }

  public TimetableCell 시간표_셀_생성(String day, int start, int end) {
    return TimetableFixture.cell(day, start, end);
  }

  // 강의

  public Lecture 강의_생성() {
    return lectureRepository.save(LectureFixture.one());
  }

  public List<Lecture> 강의_여러개_생성(int size) {
    return lectureRepository.saveAll(LectureFixture.many(size));
  }

  public LectureSchedule 강의_일정_생성(Long lectureId) {
    return lectureScheduleRepository.save(LectureFixture.schedule(lectureId));
  }

  // 공지사항

  public Notice 공지사항_생성() {
    return noticeRepository.save(new Notice("제목", "내용"));
  }

  public List<Notice> 공지사항_여러개_생성(int size) {
    return noticeRepository.saveAll(IntStream.range(0, size)
      .mapToObj(it -> new Notice("제목" + it, "내용" + it))
      .toList());
  }

  // 시험평가

  public ExamPost 시험평가_생성(Long userId, Lecture lecture) {
    return examPostRepository.save(ExamPostFixture.one(userId, lecture));
  }

  public List<ExamPost> 시험평가_여러개_생성(Long userId, Lecture lecture, int size) {
    return examPostRepository.saveAll(ExamPostFixture.many(userId, lecture, size));
  }

  public List<ExamPost> 시험평가_여러개_생성_유저_제외(Long userId, Lecture lecture, int size) {
    return examPostRepository.saveAll(ExamPostFixture.manyWithoutUser(userId, lecture, size));
  }

  // 강의평가

  public EvaluatePost 강의평가_생성(Long userId, Lecture lecture) {
    return evaluatePostRepository.save(EvaluatePostFixture.one(userId, lecture));
  }

  public List<EvaluatePost> 강의평가_여러개_생성(Long userId, Lecture lecture, int size) {
    return evaluatePostRepository.saveAll(EvaluatePostFixture.many(userId, lecture, size));
  }

  public List<EvaluatePost> 강의평가_여러개_생성_유저_제외(Long userId, Lecture lecture, int size) {
    return evaluatePostRepository.saveAll(EvaluatePostFixture.manyWithoutUser(userId, lecture, size));
  }

  // 시험평가 구매이력

  public ViewExam 시험평가_구매이력_생성(Long userId, Long lectureId) {
    return viewExamRepository.save(new ViewExam(userId, lectureId));
  }

  // 전공 즐겨찾기

  public String 전공_즐겨찾기_생성(Long userId) {
    var major = Major.values()[0].name();
    favoriteMajorRepository.save(new FavoriteMajor(userId, major));
    return major;
  }

  public List<String> 전공_즐겨찾기_여러개_생성(Long userId) { // todo: jpa로 변경하면 saveAll()로 수정
    var major = Major.values()[0].name();
    favoriteMajorRepository.save(new FavoriteMajor(userId, major));

    var major2 = Major.values()[1].name();
    favoriteMajorRepository.save(new FavoriteMajor(userId, major2));

    var major3 = Major.values()[2].name();
    favoriteMajorRepository.save(new FavoriteMajor(userId, major3));

    return List.of(major, major2, major3);
  }

  // 신고

  public EvaluatePostReport 강의평가_신고_생성(Long reporter, EvaluatePost evaluatePost) {
    return evaluateReportRepository.save(ReportFixture.evaluate(reporter, evaluatePost.getUserIdx(), evaluatePost.getId()));
  }

  public ExamPostReport 시험평가_신고_생성(Long reporter, ExamPost examPost) {
    return examReportRepository.save(ReportFixture.exam(reporter, examPost.getUserIdx(), examPost.getId()));
  }
}

package usw.suwiki.test.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import usw.suwiki.domain.exampost.ExamDetail;
import usw.suwiki.domain.exampost.ExamPost;
import usw.suwiki.domain.exampost.LectureInfo;
import usw.suwiki.domain.lecture.Lecture;

import java.util.List;
import java.util.Random;

import static usw.suwiki.test.fixture.FixtureUtils.generate;
import static usw.suwiki.test.fixture.FixtureUtils.randomIds;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExamPostFixture {
  private static final Random RANDOM = new Random();

  public static ExamPost one(Long userId, Lecture lecture) {
    return new ExamPost(userId, "시험 평가", lectureInfo(lecture), examDetail());
  }

  public static List<ExamPost> many(Long userId, Lecture lecture, int size) {
    var randomIds = randomIds(userId, size, true);
    randomIds.add(userId);
    return generate(randomIds, id -> one(id, lecture));
  }

  public static List<ExamPost> manyWithoutUser(Long userId, Lecture lecture, int size) {
    var randomIds = randomIds(userId, size, false);
    return generate(randomIds, id -> one(id, lecture));
  }

  private static LectureInfo lectureInfo(Lecture lecture) {
    var semester = lecture.getSemester().split(",")[0];
    return new LectureInfo(lecture.getId(), lecture.getName(), semester, lecture.getProfessor());
  }

  private static ExamDetail examDetail() {
    return new ExamDetail("중간고사", "PPT", "어려움");
  }
}

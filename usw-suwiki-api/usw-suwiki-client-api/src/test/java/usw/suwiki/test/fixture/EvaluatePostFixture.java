package usw.suwiki.test.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import usw.suwiki.domain.evaluatepost.EvaluatePost;
import usw.suwiki.domain.evaluatepost.LectureInfo;
import usw.suwiki.domain.evaluatepost.LectureRating;
import usw.suwiki.domain.lecture.Lecture;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EvaluatePostFixture {
  public static EvaluatePost one(Long userId, Lecture lecture) {
    return new EvaluatePost(userId, "강의 평가", lectureInfo(lecture), lectureRating());
  }

  public static List<EvaluatePost> many(Long userId, Lecture lecture, int size) {
    var randomIds = FixtureUtils.randomIds(userId, size, true);
    randomIds.add(userId);
    return FixtureUtils.generate(randomIds, id -> one(id, lecture));
  }

  public static List<EvaluatePost> manyWithoutUser(Long userId, Lecture lecture, int size) {
    var randomIds = FixtureUtils.randomIds(userId, size, false);
    return FixtureUtils.generate(randomIds, id -> one(id, lecture));
  }

  private static LectureInfo lectureInfo(Lecture lecture) {
    var semester = lecture.getSemester().split(",")[0];
    return new LectureInfo(lecture.getId(), lecture.getName(), semester, lecture.getProfessor());
  }

  private static LectureRating lectureRating() {
    return new LectureRating(1, 1, 1, 1, 1, 1);
  }
}

package usw.suwiki.api.evaluate;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import usw.suwiki.domain.evaluatepost.EvaluatePost;
import usw.suwiki.domain.evaluatepost.LectureInfo;
import usw.suwiki.domain.evaluatepost.LectureRating;
import usw.suwiki.domain.lecture.Lecture;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EvaluatePostFixture {
  private static final Random RANDOM = new Random();

  public static EvaluatePost one(Long userId, Lecture lecture) {
    return new EvaluatePost(userId, "강의 평가", lectureInfo(lecture), lectureRating());
  }

  public static List<EvaluatePost> many(Long userId, Lecture lecture, int size) {
    var randomIds = IntStream.range(0, size)
      .mapToObj(it -> RANDOM.nextLong(1000000L))
      .collect(Collectors.toSet());

    randomIds.add(userId);

    return randomIds.stream()
      .map(id -> one(id, lecture))
      .toList();
  }

  private static LectureInfo lectureInfo(Lecture lecture) {
    return new LectureInfo(lecture.getId(), lecture.getName(), lecture.getSemester(), lecture.getProfessor());
  }

  private static LectureRating lectureRating() {
    return new LectureRating(1, 1, 1, 1, 1, 1);
  }
}

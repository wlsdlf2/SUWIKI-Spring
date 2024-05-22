package usw.suwiki.api.exam;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import usw.suwiki.domain.exampost.ExamDetail;
import usw.suwiki.domain.exampost.ExamPost;
import usw.suwiki.domain.exampost.LectureInfo;
import usw.suwiki.domain.lecture.Lecture;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExamPostFixture {
  private static final Random RANDOM = new Random();

  public static ExamPost one(Long userId, Lecture lecture) {
    return new ExamPost(userId, "시험 평가", lectureInfo(lecture), examDetail());
  }

  public static List<ExamPost> many(Long userId, Lecture lecture, int size) {
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

  private static ExamDetail examDetail() {
    return new ExamDetail("중간고사", "PPT", "어려움");
  }
}

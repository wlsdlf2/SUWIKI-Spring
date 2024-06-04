package usw.suwiki.common.test.fixture;

import usw.suwiki.domain.lecture.Lecture;
import usw.suwiki.domain.lecture.LectureDetail;
import usw.suwiki.domain.lecture.schedule.LectureSchedule;

import java.util.List;
import java.util.stream.IntStream;

import static usw.suwiki.common.test.fixture.FixtureUtils.random;

public class LectureFixture {
  private static final List<String> SEMESTERS = List.of("2021-2", "2022-1", "2022-2", "2023-1", "2023-2", "2024-1");

  private LectureFixture() {
  }

  public static Lecture one() {
    return Lecture.builder()
      .semester(randomSemesters())
      .professor("교수님")
      .name("강의명")
      .majorType("교양")
      .type(Lecture.Type.values()[random(Lecture.Type.values().length)])
      .lectureDetail(LectureDetail.builder()
        .code(String.valueOf(random(1000)))
        .point(new Double[]{2.0, 3.0, 1.0}[random(3)])
        .capprType("A형(강의식 수업)")
        .diclNo("001")
        .grade(1)
        .evaluateType(LectureDetail.Evaluation.values()[random(LectureDetail.Evaluation.values().length)])
        .build())
      .build();
  }

  public static List<Lecture> many(int size) {
    return IntStream.range(0, size)
      .mapToObj(i -> one())
      .toList();
  }

  public static LectureSchedule schedule(Long lectureId) {
    return new LectureSchedule(lectureId, "미래103(월7,8),미래B102(월5,6)", "2024-1");
  }

  private static String randomSemesters() {
    return String.join(", ", SEMESTERS.subList(random(SEMESTERS.size()), SEMESTERS.size()));
  }
}

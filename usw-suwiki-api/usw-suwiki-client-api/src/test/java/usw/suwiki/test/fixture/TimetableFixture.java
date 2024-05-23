package usw.suwiki.test.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import usw.suwiki.domain.lecture.timetable.Timetable;
import usw.suwiki.domain.lecture.timetable.TimetableCell;
import usw.suwiki.domain.lecture.timetable.TimetableCellColor;

import static usw.suwiki.test.fixture.FixtureUtils.random;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimetableFixture {
  public static Timetable one(Long userId) {
    return new Timetable(userId, "시간표", 2023, "first");
  }

  public static Timetable another(Long userId) {
    return new Timetable(userId, "시간표2", 2024, "second");
  }

  public static TimetableCell cell(String day, int start, int end) {
    var colors = TimetableCellColor.values();
    return new TimetableCell("강의", "교수님", "강의실", start, end, day.toUpperCase(), colors[random(colors.length)].name());
  }
}

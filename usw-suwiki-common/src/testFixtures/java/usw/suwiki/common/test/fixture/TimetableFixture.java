package usw.suwiki.common.test.fixture;

import usw.suwiki.domain.lecture.timetable.Timetable;
import usw.suwiki.domain.lecture.timetable.TimetableCell;
import usw.suwiki.domain.lecture.timetable.TimetableCellColor;

import static usw.suwiki.common.test.fixture.FixtureUtils.random;

public class TimetableFixture {

  private TimetableFixture() {
  }

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

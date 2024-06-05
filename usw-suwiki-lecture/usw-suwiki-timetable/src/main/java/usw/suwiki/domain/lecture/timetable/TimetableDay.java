package usw.suwiki.domain.lecture.timetable;

import usw.suwiki.core.exception.ExceptionCode;
import usw.suwiki.core.exception.TimetableException;

public enum TimetableDay {
  MON,
  TUE,
  WED,
  THU,
  FRI,
  SAT,
  SUN,
  E_LEARNING;

  public static TimetableDay from(String param) {
    try {
      return Enum.valueOf(TimetableDay.class, param.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new TimetableException(ExceptionCode.INVALID_TIMETABLE_CELL_DAY);
    }
  }

  public boolean isEquals(TimetableDay other) {
    return this == other;
  }
}

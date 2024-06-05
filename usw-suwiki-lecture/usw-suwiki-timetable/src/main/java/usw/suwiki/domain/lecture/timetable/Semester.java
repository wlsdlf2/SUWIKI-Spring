package usw.suwiki.domain.lecture.timetable;

import usw.suwiki.core.exception.TimetableException;

import static usw.suwiki.core.exception.ExceptionCode.INVALID_TIMETABLE_SEMESTER;

enum Semester {
  FIRST,
  SECOND,
  SUMMER,
  WINTER,
  ;

  public static Semester from(String param) {
    try {
      return Enum.valueOf(Semester.class, param.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new TimetableException(INVALID_TIMETABLE_SEMESTER);
    }
  }
}

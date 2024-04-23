package usw.suwiki.common.test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum Table {
  // user
  USERS, USER_ISOLATION, BLACKLIST_DOMAIN, RESTRICTING_USER,

  // post
  NOTICE, VIEW_EXAM, EXAM_POST, EVALUATE_POST,

  // lecture
  LECTURE, FAVORITE_MAJOR, LECTURE_SCHEDULE, TIMETABLE, TIMETABLE_CELLS,

  // report
  EXAM_POST_REPORT, EVALUATE_POST_REPORT,

  // e.t.c
  API_LOGGER, REFRESH_TOKEN, CONFIRMATION_TOKEN, CLIENT_APP_VERSION,
  ;

  public String lower() {
    return name().toLowerCase();
  }

  public static Set<Table> toSet() {
    return Arrays.stream(Table.values()).collect(Collectors.toSet());
  }
}

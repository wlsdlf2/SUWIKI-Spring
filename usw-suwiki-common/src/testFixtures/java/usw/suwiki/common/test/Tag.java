package usw.suwiki.common.test;

import io.github.hejow.restdocs.generator.ApiTag;

public enum Tag implements ApiTag {
  TIMETABLE("시간표 API"),
  LECTURE("강의 API"),
  USER("유저 API"),
  EVALUATE_POST("강의 평가 API"),
  EXAM_POST("시험 평가 API"),
  NOTICE("공지 API"),
  REPORT("신고 API");

  private final String description;

  Tag(String description) {
    this.description = description;
  }

  @Override
  public String getName() {
    return description;
  }
}

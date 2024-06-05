package usw.suwiki.domain.lecture.timetable;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import usw.suwiki.core.exception.TimetableException;
import usw.suwiki.infra.jpa.BaseEntity;

import java.util.ArrayList;
import java.util.List;

import static usw.suwiki.core.exception.ExceptionCode.NOT_AN_AUTHOR;
import static usw.suwiki.core.exception.ExceptionCode.OVERLAPPED_TIMETABLE_CELL_SCHEDULE;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "id", column = @Column(name = "timetable_id"))
public class Timetable extends BaseEntity {
  @Column(nullable = false)
  private Long userId;

  @Column
  private String name;

  @Column
  private Integer year;

  @Enumerated(EnumType.STRING)
  private Semester semester;

  @ElementCollection
  @CollectionTable(name = "timetable_cells", joinColumns = @JoinColumn(name = "timetable_id"))
  @OrderColumn(name = "cell_idx")
  private final List<TimetableCell> cells = new ArrayList<>();

  public Timetable(Long userId, String name, Integer year, String semester) {
    this.userId = userId;
    this.name = name;
    this.year = year;
    this.semester = Semester.from(semester);
  }

  public Timetable(Long userId, String name, Integer year, String semester, List<TimetableCell> cells) {
    this(userId, name, year, semester);
    this.cells.addAll(cells);
  }

  public void update(String name, Integer year, String semester) {
    this.name = name;
    this.year = year;
    this.semester = Semester.from(semester);
  }

  public String getSemester() {
    return this.semester.name();
  }

  public void validateAuthor(Long userId) {
    if (!this.userId.equals(userId)) {
      throw new TimetableException(NOT_AN_AUTHOR);
    }
  }

  public Timetable addCell(TimetableCell cell) {
    validateOverlap(cell);
    this.cells.add(cell);
    return this;
  }

  public void updateCell(int cellIdx, TimetableCell cell) {
    this.cells.remove(cellIdx);
    addCell(cell);
  }

  public void removeCell(int cellIdx) {
    if (cells.isEmpty()) {
      return;
    }

    this.cells.remove(cellIdx);
  }

  private void validateOverlap(TimetableCell cell) {
    if (cells.stream().anyMatch(cell::isOverlap)) {
      throw new TimetableException(OVERLAPPED_TIMETABLE_CELL_SCHEDULE);
    }
  }
}

package usw.suwiki.domain.evaluatepost;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import usw.suwiki.core.exception.EvaluatePostException;
import usw.suwiki.infra.jpa.BaseEntity;

import static usw.suwiki.core.exception.ExceptionType.NOT_AN_AUTHOR;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvaluatePost extends BaseEntity {
  @Column(nullable = false)
  private Long userIdx;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  @Embedded
  private LectureInfo lectureInfo;

  @Embedded
  private LectureRating lectureRating;

  public void update(String content, String lectureName, String selectedSemester, String professor, LectureRating lectureRating) {
    this.content = content;
    this.lectureInfo = lectureInfo.update(lectureName, selectedSemester, professor);
    this.lectureRating = lectureRating;
  }

  public void validateAuthor(Long userId) {
    if (!this.userIdx.equals(userId)) {
      throw new EvaluatePostException(NOT_AN_AUTHOR);
    }
  }

  public Long getLectureId() {
    return lectureInfo.getLectureId();
  }

  public String getLectureName() {
    return lectureInfo.getLectureName();
  }

  public String getProfessor() {
    return lectureInfo.getProfessor();
  }

  public String getSelectedSemester() {
    return lectureInfo.getSelectedSemester();
  }

  public float getTotalAvg() {
    return lectureRating.getTotalAvg();
  }

  public float getSatisfaction() {
    return lectureRating.getSatisfaction();
  }

  public float getLearning() {
    return lectureRating.getLearning();
  }

  public float getHoney() {
    return lectureRating.getHoney();
  }

  public int getTeam() {
    return lectureRating.getTeam();
  }

  public int getDifficulty() {
    return lectureRating.getDifficulty();
  }

  public int getHomework() {
    return lectureRating.getHomework();
  }
}


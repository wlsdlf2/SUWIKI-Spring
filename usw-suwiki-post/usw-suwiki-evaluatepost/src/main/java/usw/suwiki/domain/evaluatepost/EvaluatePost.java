package usw.suwiki.domain.evaluatepost;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import usw.suwiki.infra.jpa.BaseEntity;

@Getter
@Entity
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
      throw new IllegalArgumentException("not an author"); // todo: 알맞는 예외 던지기
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
}

package usw.suwiki.domain.timetable.entity;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import usw.suwiki.global.BaseTimeEntity;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UNIQUE_TIMETABLE_DAY_PERIOD",
                        columnNames = {"timetable_id", "day", "startPeriod", "endPeriod"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimetableCell extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timetable_cell_id")
    private Long id;

    // (lectureName, professorName) 중복 가능
    @NotNull
    private String lectureName;     // blank 가능

    @NotNull
    private String professorName;   // blank 가능

    @Enumerated(EnumType.STRING)
    @NotNull
    private TimetableCellColor color;

    @NotNull
    private String location;    // blank 가능

    @Enumerated(EnumType.STRING)
    @NotNull
    private TimetableDay day;

    // TODO: 1~10 제약 조건
    private Integer startPeriod;
    private Integer endPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id")
    private Timetable timetable;

    @Builder
    public TimetableCell(String lectureName, String professorName, TimetableCellColor color, String location,
                         TimetableDay day, Integer startPeriod, Integer endPeriod) {
        this.lectureName = lectureName;
        this.professorName = professorName;
        this.color = color;
        this.location = location;
        this.day = day;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
    }

    // 연관관계 편의 메서드
    public void associateTimetable(Timetable timetable) {
        if (Objects.nonNull(this.timetable)) {
            this.timetable.removeCell(this);
        }
        this.timetable = timetable;
        timetable.addCell(this);
    }

    // 비즈니스 메서드

    // TODO: 교시 유효성 검증 0 < period < 15  && end > start

}

package usw.suwiki.domain.lecture;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.domain.lecture.dto.LectureSearchOption;
import usw.suwiki.domain.lecture.model.Lectures;

import java.util.List;
import java.util.Optional;

import static usw.suwiki.domain.lecture.QLecture.lecture;

@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LectureQueryRepository {
  private static final Integer DEFAULT_LIMIT = 10;

  private final JPAQueryFactory queryFactory;

  public Optional<Lecture> findByExtraKeys(String lectureName, String professor, String major, String dividedClassNumber) {
    return Optional.ofNullable(queryFactory.selectFrom(lecture)
      .where(lecture.name.eq(lectureName),
        lecture.professor.eq(professor),
        lecture.majorType.eq(major),
        lecture.lectureDetail.diclNo.eq(dividedClassNumber))
      .fetchOne());
  }

  public Lectures findAllLectures(String keyword, LectureSearchOption option) {
    var whereCondition = new BooleanBuilder()
      .and(option.getMajor() == null ? null : lecture.majorType.eq(option.getMajor()))
      .and(keyword == null ? null : lecture.name.likeIgnoreCase("%" + keyword + "%").or(lecture.professor.likeIgnoreCase("%" + keyword + "%")));

    var result = queryFactory.selectFrom(lecture)
      .where(whereCondition)
      .orderBy(
        countOption(),
        orderSpecifier(option.getOrder())
      )
      .offset(option.getPage() * DEFAULT_LIMIT)
      .limit(DEFAULT_LIMIT)
      .fetch();

    long count = queryFactory.selectFrom(lecture)
      .where(whereCondition)
      .fetch().size();

    return new Lectures(result, count);
  }

  public List<String> findAllMajorTypes() {
    return queryFactory.selectDistinct(lecture.majorType)
      .from(lecture)
      .fetch();
  }

  private OrderSpecifier<?> orderSpecifier(String option) {
    return switch (option) {
      case "satisfaction" -> lecture.lectureEvaluationInfo.lectureSatisfactionAvg.desc();
      case "honey" -> lecture.lectureEvaluationInfo.lectureHoneyAvg.desc();
      case "learning" -> lecture.lectureEvaluationInfo.lectureLearningAvg.desc();
      case "average" -> lecture.lectureEvaluationInfo.lectureTotalAvg.desc();
      default -> lecture.modifiedDate.desc();
    };
  }

  private OrderSpecifier<Integer> countOption() {
    return new CaseBuilder().when(lecture.postsCount.gt(0)).then(1).otherwise(2).asc();
  }
}

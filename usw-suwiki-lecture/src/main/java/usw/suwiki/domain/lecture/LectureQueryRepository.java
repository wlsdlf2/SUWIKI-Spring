package usw.suwiki.domain.lecture;

import com.querydsl.core.QueryResults;
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
  private static final String DEFAULT_ORDER = "modifiedDate";
  private static final Integer DEFAULT_LIMIT = 10;
  private static final Integer DEFAULT_PAGE = 1;

  private final JPAQueryFactory queryFactory;

  public Optional<Lecture> findByExtraKeys(String lectureName, String professor, String major, String dividedClassNumber) {
    return Optional.ofNullable(queryFactory.selectFrom(lecture)
      .where(lecture.name.eq(lectureName),
        lecture.professor.eq(professor),
        lecture.majorType.eq(major),
        lecture.lectureDetail.diclNo.eq(dividedClassNumber))
      .fetchOne());
  }

  public Lectures findAllLecturesByOption(String searchValue, LectureSearchOption option) {
    QueryResults<Lecture> queryResults = queryFactory
      .selectFrom(lecture)
      .where(lecture.name.likeIgnoreCase("%" + searchValue + "%")
        .or(lecture.professor.likeIgnoreCase("%" + searchValue + "%")))
      .orderBy(
        createPostCountOption(),
        orderSpecifier(option.getOrder())
      )
      .offset((page(option.getPage()) - 1) * DEFAULT_LIMIT)
      .limit(DEFAULT_LIMIT)
      .fetchResults();

    return new Lectures(queryResults.getResults(), queryResults.getTotal());
  }

  public Lectures findAllLecturesByOption(LectureSearchOption option) {
    QueryResults<Lecture> queryResults = queryFactory
      .selectFrom(lecture)
      .orderBy(
        createPostCountOption(),
        orderSpecifier(option.getOrder())
      )
      .offset((page(option.getPage()) - 1) * DEFAULT_LIMIT)
      .limit(DEFAULT_LIMIT)
      .fetchResults();

    long count = queryFactory
      .selectFrom(lecture)
      .fetchCount();

    return new Lectures(queryResults.getResults(), count);
  }

  public Lectures findAllLecturesByMajorType(LectureSearchOption option) {
    QueryResults<Lecture> queryResults = queryFactory
      .selectFrom(lecture)
      .where(option.getMajor() == null ? null : lecture.majorType.eq(option.getMajor()))
      .orderBy(
        createPostCountOption(),
        orderSpecifier(option.getOrder())
      )
      .offset((page(option.getPage()) - 1) * DEFAULT_LIMIT)
      .limit(DEFAULT_LIMIT)
      .fetchResults();

    long count = queryFactory
      .selectFrom(lecture)
      .where(option.getMajor() == null ? null : lecture.majorType.eq(option.getMajor()))
      .fetchCount();

    return new Lectures(queryResults.getResults(), count);
  }

  public Lectures findAllLecturesByMajorType(String keyword, LectureSearchOption option) {
    var condition = option.getMajor() == null ? null : lecture.majorType.eq(option.getMajor())
      .and(lecture.name.likeIgnoreCase("%" + keyword + "%")
        .or(lecture.professor.likeIgnoreCase("%" + keyword + "%")));

    QueryResults<Lecture> queryResults = queryFactory
      .selectFrom(lecture)
      .where(condition)
      .orderBy(
        createPostCountOption(),
        orderSpecifier(option.getOrder())
      )
      .offset((page(option.getPage()) - 1) * DEFAULT_LIMIT)
      .limit(DEFAULT_LIMIT)
      .fetchResults();

    long count = queryFactory
      .selectFrom(lecture)
      .where(condition)
      .fetchCount();

    return new Lectures(queryResults.getResults(), count);
  }

  public List<String> findAllMajorTypes() {
    return queryFactory.selectDistinct(lecture.majorType)
      .from(lecture)
      .fetch();
  }

  private OrderSpecifier<?> orderSpecifier(String option) {
    return switch (orderOption(option)) {
      case "lectureEvaluationInfo.lectureSatisfactionAvg" ->
        lecture.lectureEvaluationInfo.lectureSatisfactionAvg.desc();
      case "lectureEvaluationInfo.lectureHoneyAvg" -> lecture.lectureEvaluationInfo.lectureHoneyAvg.desc();
      case "lectureEvaluationInfo.lectureLearningAvg" -> lecture.lectureEvaluationInfo.lectureLearningAvg.desc();
      case "lectureEvaluationInfo.lectureTotalAvg" -> lecture.lectureEvaluationInfo.lectureTotalAvg.desc();
      default -> lecture.modifiedDate.desc(); // Default order
    };
  }

  private String orderOption(String option) {
    if (option == null) {
      return DEFAULT_ORDER;
    }

    return option.equals(DEFAULT_ORDER) ? option : "lectureEvaluationInfo." + option;
  }

  private OrderSpecifier<Integer> createPostCountOption() {
    return new CaseBuilder().when(lecture.postsCount.gt(0)).then(1).otherwise(2).asc();
  }

  private long page(Long page) {
    return page == null ? DEFAULT_PAGE : page;
  }
}

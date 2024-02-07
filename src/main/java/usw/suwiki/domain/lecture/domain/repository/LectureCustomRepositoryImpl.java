package usw.suwiki.domain.lecture.domain.repository;

import static usw.suwiki.domain.lecture.domain.QLecture.lecture;
import static usw.suwiki.domain.lecture.domain.QLectureSchedule.lectureSchedule;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import usw.suwiki.domain.lecture.controller.dto.LectureFindOption;
import usw.suwiki.domain.lecture.domain.Lecture;
import usw.suwiki.domain.lecture.domain.LectureSchedule;
import usw.suwiki.domain.lecture.domain.repository.dao.LecturesAndCountDao;
import usw.suwiki.global.util.query.SlicePaginationUtils;


@RequiredArgsConstructor
public class LectureCustomRepositoryImpl implements LectureCustomRepository { // TODO style: Repository명 변경

    private final JPAQueryFactory queryFactory;
    private final String DEFAULT_ORDER = "modifiedDate";
    private final Integer DEFAULT_PAGE = 1;
    private final Integer DEFAULT_LIMIT = 10;

    @Value("${business.current-semester}")
    private String currentSemester; // TODO 고민: Lecture - currently_opened 혹은 last_opened_semester 컬럼 추가 -> 데이터 파싱 로직 및 WHERE절 변경해야 함.

    // TODO fix: LectureSchedule을 Lecture과 조인해서 조회해야 함. 단, where절 orderby절 등 기준은 lecture가 되어야 함.
    @Override
    public Slice<LectureSchedule> findCurrentSemesterLectureSchedules(
            final Long cursorId,
            final int limit,
            final String keyword,
            final String majorType,
            final Integer grade
    ) {
        JPAQuery<LectureSchedule> query = queryFactory.selectFrom(lectureSchedule)
                .join(lectureSchedule.lecture)
                .where(gtLectureScheduleLectureCursorId(cursorId))
                .where(containsLectureScheduleLectureKeyword(keyword))
                .where(eqLectureScheduleLectureMajorType(majorType))
                .where(eqLectureScheduleLectureGrade(grade))
                .where(lectureSchedule.lecture.semester.endsWith(currentSemester))
                .orderBy(lectureSchedule.lecture.id.asc())
                .limit(SlicePaginationUtils.increaseSliceLimit(limit));

        return SlicePaginationUtils.buildSlice(query.fetch(), limit);
    }


    @Override
    public Optional<Lecture> findByExtraUniqueKey(String lectureName, String professorName, String majorType) {
        Lecture result = queryFactory
                .selectFrom(lecture)
                .where(
                        lecture.name.eq(lectureName),
                        lecture.professor.eq(professorName),
                        lecture.majorType.eq(majorType)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }


    /**
     * if (!Arrays.asList(orderOptions).contains(orderOption)) { throw new
     * AccountException(ExceptionType.INVALID_ORDER_OPTION); }
     */
    @Override
    public LecturesAndCountDao findLectureByFindOption(String searchValue, LectureFindOption option) {
        String orderOption = initializeOrderOption(option.getOrderOption());
        Integer page = initializePageNumber(option.getPageNumber());

        BooleanExpression searchCondition = lecture.name
                .likeIgnoreCase("%" + searchValue + "%")
                .or(lecture.professor.likeIgnoreCase("%" + searchValue + "%"));

        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(orderOption);

        Pageable pageable = PageRequest.of(page - 1, DEFAULT_LIMIT);
        QueryResults<Lecture> queryResults = queryFactory
                .selectFrom(lecture)
                .where(searchCondition)
                .orderBy(
                        createPostCountOption(),
                        orderSpecifier
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        Long count = queryResults.getTotal();

        return LecturesAndCountDao.builder()
                .lectureList(queryResults.getResults())
                .count(count)
                .build();
    }

    @Override
    public LecturesAndCountDao findLectureByMajorType(String searchValue, LectureFindOption option) {
        String orderOption = initializeOrderOption(option.getOrderOption());
        Integer page = initializePageNumber(option.getPageNumber());
        String majorType = option.getMajorType();

        BooleanExpression searchCondition = lecture.majorType.eq(majorType)
                .and(lecture.name.likeIgnoreCase("%" + searchValue + "%")
                        .or(lecture.professor.likeIgnoreCase("%" + searchValue + "%")));

        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(orderOption);

        QueryResults<Lecture> queryResults = queryFactory
                .selectFrom(lecture)
                .where(searchCondition)
                .orderBy(
                        createPostCountOption(),
                        orderSpecifier
                )
                .offset((long) (page - 1) * DEFAULT_LIMIT)
                .limit(DEFAULT_LIMIT)
                .fetchResults();

        long count = queryFactory
                .selectFrom(lecture)
                .where(searchCondition)
                .fetchCount();

        return LecturesAndCountDao.builder()
                .lectureList(queryResults.getResults())
                .count(count)
                .build();
    }


    @Override
    public LecturesAndCountDao findAllLectureByFindOption(LectureFindOption option) {
        String orderOption = initializeOrderOption(option.getOrderOption());
        Integer page = initializePageNumber(option.getPageNumber());

        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(orderOption);

        QueryResults<Lecture> queryResults = queryFactory
                .selectFrom(lecture)
                .orderBy(
                        createPostCountOption(),
                        orderSpecifier
                )
                .offset((long) (page - 1) * DEFAULT_LIMIT)
                .limit(DEFAULT_LIMIT)
                .fetchResults();

        long count = queryFactory
                .selectFrom(lecture)
                .fetchCount();

        return LecturesAndCountDao.builder()
                .lectureList(queryResults.getResults())
                .count(count)
                .build();
    }

    @Override
    public LecturesAndCountDao findAllLectureByMajorType(LectureFindOption option) {
        String orderOption = initializeOrderOption(option.getOrderOption());
        Integer page = initializePageNumber(option.getPageNumber());
        String majorType = option.getMajorType();

        BooleanExpression searchCondition = lecture.majorType.eq(majorType);
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(orderOption);

        QueryResults<Lecture> queryResults = queryFactory
                .selectFrom(lecture)
                .where(searchCondition)
                .orderBy(
                        createPostCountOption(),
                        orderSpecifier
                )
                .offset((long) (page - 1) * DEFAULT_LIMIT)
                .limit(DEFAULT_LIMIT)
                .fetchResults();

        long count = queryFactory
                .selectFrom(lecture)
                .where(searchCondition)
                .fetchCount();

        return LecturesAndCountDao.builder()
                .lectureList(queryResults.getResults())
                .count(count)
                .build();
    }

    @Override
    public List<String> findAllMajorType() {
        return queryFactory
                .selectDistinct(lecture.majorType)
                .from(lecture)
                .fetch();
    }

    private BooleanExpression gtLectureScheduleLectureCursorId(Long cursorId) {
        if (Objects.isNull(cursorId)) {
            return null;
        }
        return lectureSchedule.lecture.id.gt(cursorId);
    }

    private BooleanExpression containsLectureScheduleLectureKeyword(String keyword) {
        if (Objects.isNull(keyword)) {
            return null;
        }
        return lectureSchedule.lecture.name.contains(keyword)
                .or(lectureSchedule.lecture.professor.contains(keyword));
    }

    private BooleanExpression eqLectureScheduleLectureMajorType(String majorType) {
        if (Objects.isNull(majorType)) {
            return null;
        }
        return lectureSchedule.lecture.majorType.eq(majorType);
    }

    private BooleanExpression eqLectureScheduleLectureGrade(Integer grade) {
        if (Objects.isNull(grade)) {
            return null;
        }
        return lectureSchedule.lecture.lectureDetail.grade.eq(grade);
    }

    private OrderSpecifier<?> getOrderSpecifier(String orderOption) {
        switch (orderOption) {
            case "lectureEvaluationInfo.lectureSatisfactionAvg":
                return lecture.lectureEvaluationInfo.lectureSatisfactionAvg.desc();
            case "lectureEvaluationInfo.lectureHoneyAvg":
                return lecture.lectureEvaluationInfo.lectureHoneyAvg.desc();
            case "lectureEvaluationInfo.lectureLearningAvg":
                return lecture.lectureEvaluationInfo.lectureLearningAvg.desc();
            case "lectureEvaluationInfo.lectureTotalAvg":
                return lecture.lectureEvaluationInfo.lectureTotalAvg.desc();
            default:
                return lecture.modifiedDate.desc(); // Default order
        }
    }

    private OrderSpecifier<Integer> createPostCountOption() {
        return new CaseBuilder()
                .when(lecture.postsCount.gt(0)).then(1)
                .otherwise(2)
                .asc();
    }

    private String initializeOrderOption(String option) {
        if (option == null) {
            return DEFAULT_ORDER;
        }
        if (option.equals(DEFAULT_ORDER)) {
            return option;
        }
        return "lectureEvaluationInfo." + option;
    }

    private Integer initializePageNumber(Integer page) {
        if (page == null) {
            page = DEFAULT_PAGE;
        }
        return page;
    }
}
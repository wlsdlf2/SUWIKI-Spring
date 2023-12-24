package usw.suwiki.repository.timetable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import usw.suwiki.config.TestJpaConfig;
import usw.suwiki.domain.timetable.entity.Semester;
import usw.suwiki.domain.timetable.entity.Timetable;
import usw.suwiki.domain.timetable.repository.TimetableRepository;
import usw.suwiki.domain.user.user.User;
import usw.suwiki.domain.user.user.repository.UserRepository;
import usw.suwiki.template.user.UserTemplate;

@DataJpaTest
@Import(TestJpaConfig.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class TimetableRepositoryTest {

    @Autowired
    private TimetableRepository timetableRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private User dummyUser;
    private Timetable dummyTimetable;

    @BeforeEach
    void setUp() {
        this.dummyUser = userRepository.save(UserTemplate.createDummyUser());

        Timetable timetable = Timetable.builder()
                .user(dummyUser)
                .name("내 시간표")
                .year(2023)
                .semester(Semester.SECOND)
                .build();
        this.dummyTimetable = timetableRepository.save(timetable);
    }

    @Test
    @DisplayName("INSERT Timetable 성공")
    public void insertTimetable_success() {
        // given
        Timetable validTimetable = Timetable.builder()
                .user(dummyUser)
                .name("첫 학기")
                .year(2017)
                .semester(Semester.FIRST)
                .build();

        // when & then
        assertThatNoException().isThrownBy(() -> timetableRepository.save(validTimetable));

        userRepository.delete(dummyUser);
    }

    @Test
    @DisplayName("INSERT Timetable 성공 - User 연관관계 메서드")
    public void insertTimetable_success_user_association_method() { // TODO: remove 연관관계 메서드 테스트
        // given
        Timetable validTimetable = Timetable.builder()
                .name("첫 학기")
                .year(2017)
                .semester(Semester.FIRST)
                .build();

        // when
        dummyUser.addTimetable(validTimetable);
        testEntityManager.persist(dummyUser);   // 유저 영속화
        testEntityManager.flush();

        // then
        Optional<Timetable> byId = timetableRepository.findById(validTimetable.getId());    // persist -> id 생김

        assertThat(byId.isPresent()).isTrue();
        assertThat(byId.get()).isEqualTo(validTimetable);
    }

    @Test
    @DisplayName("INSERT Timetable 실패 - NOT NULL 제약조건 위반")
    public void insertTimetable_fail_notnull_constraint() {
        // given
        Timetable nullNameTimetable = Timetable.builder()
                .user(dummyUser)
                .name(null)
                .year(2017)
                .semester(Semester.FIRST)
                .build();

        Timetable nullYearTimetable = Timetable.builder()
                .user(dummyUser)
                .name("첫 학기")
                .year(null)
                .semester(Semester.FIRST)
                .build();

        Timetable nullSemesterTimetable = Timetable.builder()
                .user(dummyUser)
                .name("첫 학기")
                .year(2017)
                .semester(null)
                .build();

        // when & then
        assertThatThrownBy(() -> timetableRepository.save(nullNameTimetable))
                .isExactlyInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(() -> timetableRepository.save(nullYearTimetable))
                .isExactlyInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(() -> timetableRepository.save(nullSemesterTimetable))
                .isExactlyInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("SELECT Timetable id 조회 성공")
    public void selectTimetableById_success() {
        // given
        Long id = dummyTimetable.getId();

        // when
        Optional<Timetable> optionalTimetable = timetableRepository.findById(id);

        // then
        assertThat(optionalTimetable.isPresent()).isTrue();
        assertThat(optionalTimetable.get()).isEqualTo(dummyTimetable);
    }

    @Test
    @DisplayName("SELECT Timetable userId 조회 성공")
    public void selectTimetableByUserId_success() {
        // given
        Long userId = dummyUser.getId();

        // when
        Optional<Timetable> optionalTimetable = timetableRepository.findByUserId(userId);

        // then
        assertThat(optionalTimetable.isPresent()).isTrue();
        assertThat(optionalTimetable.get()).isEqualTo(dummyTimetable);
    }

}

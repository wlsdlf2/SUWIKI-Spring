package usw.suwiki.domain.viewexam;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ViewExamRepository extends JpaRepository<ViewExam, Long>, CustomViewExamRepository {
}

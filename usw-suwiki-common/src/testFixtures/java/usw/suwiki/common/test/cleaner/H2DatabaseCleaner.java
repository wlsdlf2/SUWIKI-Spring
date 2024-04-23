package usw.suwiki.common.test.cleaner;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import usw.suwiki.common.test.Table;
import usw.suwiki.common.test.support.DatabaseCleaner;

import java.util.Set;

@Profile("h2")
@Component
class H2DatabaseCleaner implements DatabaseCleaner {
  private static final String OFF_REFERENTIAL_INTEGRITY = "SET REFERENTIAL_INTEGRITY FALSE";
  private static final String ON_REFERENTIAL_INTEGRITY = "SET REFERENTIAL_INTEGRITY TRUE";
  private static final String TRUNCATE = "TRUNCATE TABLE ";

  @PersistenceContext
  protected EntityManager entityManager;

  @Override
  @Transactional
  public void clean(Set<Table> tables) {
    entityManager.createNativeQuery(OFF_REFERENTIAL_INTEGRITY).executeUpdate();
    tables.forEach(table -> entityManager.createNativeQuery(TRUNCATE + table.lower()).executeUpdate());
    entityManager.createNativeQuery(ON_REFERENTIAL_INTEGRITY).executeUpdate();
  }
}

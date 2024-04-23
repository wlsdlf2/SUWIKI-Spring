package usw.suwiki.common.test.support;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import usw.suwiki.common.test.Table;

public class AcceptanceTestExecutionListener implements TestExecutionListener {

  @Override
  public void afterTestMethod(TestContext testContext) {
    var databaseCleaner = testContext.getApplicationContext().getBean(DatabaseCleaner.class);
    databaseCleaner.clean(Table.toSet());
  }
}

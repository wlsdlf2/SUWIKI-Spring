package usw.suwiki.common.test.support;

import org.springframework.test.context.ActiveProfilesResolver;
import usw.suwiki.common.test.annotation.AcceptanceTest;

public class TestProfilesResolver implements ActiveProfilesResolver {

  @Override
  public String[] resolve(Class<?> testClass) {
    var acceptanceTest = testClass.getAnnotation(AcceptanceTest.class);
    var testDataBase = acceptanceTest.testDatabase();
    return new String[]{testDataBase.name().toLowerCase()};
  }
}

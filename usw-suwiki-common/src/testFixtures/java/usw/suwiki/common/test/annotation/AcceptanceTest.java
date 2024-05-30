package usw.suwiki.common.test.annotation;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import usw.suwiki.common.test.support.AcceptanceTestExecutionListener;
import usw.suwiki.common.test.support.TestProfilesResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ActiveProfiles(resolver = TestProfilesResolver.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(value = AcceptanceTestExecutionListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public @interface AcceptanceTest {
  enum TestDatabase {H2, MYSQL}

  TestDatabase testDatabase() default TestDatabase.H2;
}

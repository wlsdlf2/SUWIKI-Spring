package usw.suwiki.common.test.support;

import org.springframework.data.util.Pair;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import usw.suwiki.common.test.extension.AssertExtension;
import usw.suwiki.core.exception.ExceptionCode;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class ResponseValidator {

  private ResponseValidator() {
  }

  @SafeVarargs
  public static void validate(ResultActions result, ResultMatcher expectedStatus, Pair<String, Object>... expects) throws Exception {
    result.andExpectAll(
      Stream.concat(
        Stream.of(expectedStatus),
        Arrays.stream(expects).map(expect -> jsonPath(expect.getFirst()).value(expect.getSecond()))
      ).toArray(ResultMatcher[]::new)
    );
  }

  public static void validate(ResultActions result, ResultMatcher expectedStatus, ExceptionCode expectedException) throws Exception {
    result.andExpectAll(
      expectedStatus,
      AssertExtension.expectExceptionJsonPath(result, expectedException)
    );
  }

  public static void validateHtml(ResultActions result, ResultMatcher expectedStatus, Object expected) throws Exception {
    result.andExpectAll(
      expectedStatus,
      content().string(expected.toString())
    );
  }
}

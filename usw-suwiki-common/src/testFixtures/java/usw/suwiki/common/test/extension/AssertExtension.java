package usw.suwiki.common.test.extension;

import org.assertj.core.api.ThrowableAssert;
import org.assertj.core.api.ThrowableAssertAlternative;
import org.assertj.core.api.ThrowableTypeAssert;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import usw.suwiki.core.exception.BaseException;
import usw.suwiki.core.exception.ExceptionCode;

import java.util.Objects;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public final class AssertExtension {

  public static ResultMatcher expectExceptionJsonPath(ResultActions resultActions, ExceptionCode exceptionCode) {
    return result -> Objects.requireNonNull(resultActions).andExpectAll(
      jsonPath("$.code").value(exceptionCode.getCode()),
      jsonPath("$.message").value(exceptionCode.getMessage()),
      jsonPath("$.status").value(exceptionCode.getStatus())
    );
  }

  public static CustomThrowableTypeAssert assertThatApplicationException(ExceptionCode exceptionCode) {
    return new CustomThrowableTypeAssert(BaseException.class, exceptionCode);
  }

  public static class CustomThrowableTypeAssert extends ThrowableTypeAssert<BaseException> {
    private final ExceptionCode exceptionCode;

    private CustomThrowableTypeAssert(Class<? extends BaseException> throwableType, ExceptionCode exceptionCode) {
      super(throwableType);
      this.exceptionCode = exceptionCode;
    }

    @Override
    public ThrowableAssertAlternative<BaseException> isThrownBy(ThrowableAssert.ThrowingCallable throwingCallable) {
      return super.isThrownBy(throwingCallable)
        .withMessage(exceptionCode.getMessage());
    }
  }
}

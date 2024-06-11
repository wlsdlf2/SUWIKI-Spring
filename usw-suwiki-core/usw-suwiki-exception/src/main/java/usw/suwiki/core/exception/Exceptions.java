package usw.suwiki.core.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Exceptions {
  private static final String SUB_LOG_PREFIX = "at ";
  private static final String NEW_LINE = "\n";

  private static final List<String> IGNORABLE_PACKAGES = List.of(
    "org.springframework.aop", "com.fasterxml.jackson",
    "java.base", "org.hibernate", "org.apache",
    "com.sun", "jakarta.servlet", "jdk.internal"
  );

  public static String simplify(Throwable throwable) {
    Objects.requireNonNull(throwable, "throwable cannot be null!");
    var stackTrace = getStackTrace(throwable);
    return truncate(stackTrace);
  }

  private static String getStackTrace(Throwable throwable) {
    var sw = new StringWriter();
    var pw = new PrintWriter(sw, true);
    throwable.printStackTrace(pw);
    return sw.getBuffer().toString();
  }

  private static String truncate(String message) {
    return Arrays.stream(message.split(NEW_LINE))
      .filter(Exceptions::isNecessary)
      .collect(Collectors.joining(NEW_LINE));
  }

  private static boolean isNecessary(String log) {
    return !(log.contains(SUB_LOG_PREFIX) && IGNORABLE_PACKAGES.stream().anyMatch(log::contains));
  }
}

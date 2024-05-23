package usw.suwiki.test.fixture;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class FixtureUtils {
  private static final Random RANDOM = new Random();

  private static final long MAX_BOUND = 1000000L;

  private FixtureUtils() {
  }

  public static int random(int bound) {
    return RANDOM.nextInt(bound);
  }

  public static Set<Long> randomIds(Long target, int size, boolean include) {
    return Stream.generate(() -> RANDOM.nextLong(MAX_BOUND))
      .filter(it -> !it.equals(target))
      .distinct()
      .limit(include ? size - 1 : size)
      .collect(Collectors.toSet());
  }

  public static <T, E> List<T> generate(Collection<E> collection, Function<E, T> mapper) {
    return collection.stream().map(mapper).toList();
  }
}

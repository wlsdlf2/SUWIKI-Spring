package usw.suwiki.core.secure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class RandomPasswordGeneratorTest {
  private static final String[] SYMBOLS = {"!", "@", "#", "$", "%", "^"};

  @Test
  void 비밀번호_생성_성공_8자리_이하의_특수문자_포함() {
    // given

    // when
    var password = RandomPasswordGenerator.generate();

    // then
    assertAll(
      () -> assertThat(password).isNotBlank(),
      () -> assertThat(password).hasSize(8),
      () -> assertThat(password).containsAnyOf(SYMBOLS)
    );
  }
}

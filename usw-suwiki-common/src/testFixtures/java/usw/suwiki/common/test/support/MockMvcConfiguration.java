package usw.suwiki.common.test.support;

import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.CharacterEncodingFilter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@TestConfiguration
public class MockMvcConfiguration {

  @Bean
  public MockMvcBuilderCustomizer mockMvcBuilderCustomizer() {
    return builder -> builder
      .alwaysDo(print())
      .addFilter(new CharacterEncodingFilter(UTF_8.displayName(), true))
      .build();
  }
}

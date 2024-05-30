package usw.suwiki.common.test.support;

import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.web.filter.CharacterEncodingFilter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@TestConfiguration
public class MockMvcConfiguration {
  private static final String BASE_PATH = "/**";

  @Bean
  public MockMvcBuilderCustomizer mockMvcBuilderCustomizer() {
    return builder -> builder
      .alwaysDo(print())
      .addFilter(new CharacterEncodingFilter(UTF_8.displayName(), true))
      .defaultRequest(RestDocumentationRequestBuilders.post(BASE_PATH).with(csrf().asHeader()))
      .defaultRequest(RestDocumentationRequestBuilders.get(BASE_PATH).with(csrf().asHeader()))
      .defaultRequest(RestDocumentationRequestBuilders.put(BASE_PATH).with(csrf().asHeader()))
      .defaultRequest(RestDocumentationRequestBuilders.patch(BASE_PATH).with(csrf().asHeader()))
      .defaultRequest(RestDocumentationRequestBuilders.delete(BASE_PATH).with(csrf().asHeader()))
      .build();
  }
}

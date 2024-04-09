package usw.suwiki.comon.test.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import usw.suwiki.comon.test.db.Table;

import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension.class)
public abstract class WebMvcTestSupport {
  private static final String ANY_END_POINT = "/**";

  protected final String INVALID_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
                                                ".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ" +
                                                ".SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected DatabaseCleaner databaseCleaner;

  protected MockMvc mockMvc;

  protected abstract Set<Table> targetTables();

  protected abstract void clean();

  @BeforeEach
  void setup(
    WebApplicationContext webApplicationContext,
    RestDocumentationContextProvider restDocumentation
  ) {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
      .alwaysDo(print())
      .apply(documentationConfiguration(restDocumentation))
      .addFilter(new CharacterEncodingFilter(UTF_8.displayName(), true))
      .defaultRequest(post(ANY_END_POINT).with(csrf().asHeader()))
      .defaultRequest(get(ANY_END_POINT).with(csrf().asHeader()))
      .defaultRequest(put(ANY_END_POINT).with(csrf().asHeader()))
      .defaultRequest(delete(ANY_END_POINT).with(csrf().asHeader()))
      .build();
  }
}

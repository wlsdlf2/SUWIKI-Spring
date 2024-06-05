package usw.suwiki.common.test.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import usw.suwiki.common.test.HttpMethod;
import usw.suwiki.common.test.fixture.Fixtures;

import java.util.Arrays;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(MockMvcConfiguration.class)
@ExtendWith(RestDocumentationExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public abstract class AcceptanceTestSupport {
  protected final String INVALID_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  protected Fixtures fixtures;

  public ResultActions get(Uri uri, String accessToken) throws Exception {
    return perform(uri, accessToken, null);
  }

  public ResultActions get(Uri uri, Pair... parameters) throws Exception {
    return perform(uri, null, parameters);
  }

  public ResultActions get(Uri uri, String accessToken, Pair... parameters) throws Exception {
    return perform(uri, accessToken, parameters);
  }

  public ResultActions getHtml(Uri uri, Pair... parameters) throws Exception {
    return performNonJson(uri, null, parameters);
  }

  // for auth
  public ResultActions post(Uri uri, Cookie cookie) throws Exception {
    return mockMvc.perform(RestDocumentationRequestBuilders.post(uri.resource)
      .cookie(cookie)
      .contentType(MediaType.APPLICATION_JSON)
      .requestAttr("org.springframework.restdocs.urlTemplate", uri.urlTemplate));
  }

  public ResultActions post(Uri uri, String accessToken) throws Exception {
    return perform(uri, HttpMethod.POST, accessToken, null);
  }

  public ResultActions post(Uri uri, Object requestBody) throws Exception {
    return perform(uri, HttpMethod.POST, null, requestBody);
  }

  public ResultActions post(Uri uri, String accessToken, Object requestBody) throws Exception {
    return perform(uri, HttpMethod.POST, accessToken, requestBody);
  }

  // todo: post query string이 어색하므로 삭제 예정
  public ResultActions post(Uri uri, String accessToken, Object requestBody, Pair... parameters) throws Exception {
    return perform(uri, HttpMethod.POST, accessToken, requestBody, parameters);
  }

  public ResultActions put(Uri uri, Object requestBody) throws Exception {
    return perform(uri, HttpMethod.PUT, null, requestBody);
  }

  public ResultActions put(Uri uri, String accessToken, Object requestBody, Pair... parameters) throws Exception {
    return perform(uri, HttpMethod.PUT, accessToken, requestBody, parameters);
  }

  public ResultActions put(Uri uri, String accessToken, Object requestBody) throws Exception {
    return perform(uri, HttpMethod.PUT, accessToken, requestBody);
  }

  public ResultActions patch(Uri uri, Object requestBody) throws Exception {
    return perform(uri, HttpMethod.PATCH, null, requestBody);
  }

  public ResultActions patch(Uri uri, String accessToken, Object requestBody) throws Exception {
    return perform(uri, HttpMethod.PATCH, accessToken, requestBody);
  }

  public ResultActions delete(Uri uri, String accessToken) throws Exception {
    return perform(uri, HttpMethod.DELETE, accessToken, null);
  }

  public ResultActions delete(Uri uri, String accessToken, Object requestBody) throws Exception {
    return perform(uri, HttpMethod.DELETE, accessToken, requestBody);
  }

  // todo: null 유도로 삭제 예정
  public ResultActions delete(Uri uri, String accessToken, Object requestBody, Pair... parameters) throws Exception {
    return perform(uri, HttpMethod.DELETE, accessToken, requestBody, parameters);
  }

  /**
   * query 전용 acceptance test template
   */
  private ResultActions perform(Uri uri, String accessToken, Pair... parameters) throws Exception {
    var request = toRequestBuilder(uri, HttpMethod.GET, false);

    return perform(parameters != null ? request.queryParams(toParams(parameters)) : request, accessToken);
  }

  private ResultActions performNonJson(Uri uri, String accessToken, Pair... parameters) throws Exception {
    var request = toRequestBuilder(uri, HttpMethod.GET, true);

    return perform(parameters != null ? request.queryParams(toParams(parameters)) : request, accessToken);
  }

  private MultiValueMap<String, String> toParams(Pair... parameters) {
    return Arrays.stream(parameters).collect(
      LinkedMultiValueMap::new,
      (map, pair) -> map.add(pair.first(), pair.second()),
      LinkedMultiValueMap::addAll
    );
  }

  /**
   * command 전용 acceptance test template
   */
  private ResultActions perform(Uri uri, HttpMethod httpMethod, String accessToken, Object requestBody) throws Exception {
    var request = toRequestBuilder(uri, httpMethod, false);

    return perform(requestBody != null ? request.content(objectMapper.writeValueAsString(requestBody)) : request, accessToken);
  }

  private ResultActions perform(Uri uri, HttpMethod httpMethod, String accessToken, Object requestBody, Pair... parameters) throws Exception {
    var request = toRequestBuilder(uri, httpMethod, false);

    if (requestBody != null) {
      request.content(objectMapper.writeValueAsString(requestBody));
    }

    return perform(parameters != null ? request.queryParams(toParams(parameters)) : request, accessToken);
  }

  private MockHttpServletRequestBuilder toRequestBuilder(Uri uri, HttpMethod httpMethod, boolean html) {
    var request = switch (httpMethod) {
      case GET -> RestDocumentationRequestBuilders.get(uri.resource);
      case POST -> RestDocumentationRequestBuilders.post(uri.resource);
      case PUT -> RestDocumentationRequestBuilders.put(uri.resource);
      case PATCH -> RestDocumentationRequestBuilders.patch(uri.resource);
      case DELETE -> RestDocumentationRequestBuilders.delete(uri.resource);
    };

    return request.accept(html ? MediaType.TEXT_HTML : MediaType.APPLICATION_JSON)
      .contentType(html ? MediaType.TEXT_HTML : MediaType.APPLICATION_JSON)
      .requestAttr("org.springframework.restdocs.urlTemplate", uri.urlTemplate); // rest-docs path variable 설정
  }

  /**
   * 결과적으로 요청을 호출하는 method
   */
  private ResultActions perform(MockHttpServletRequestBuilder request, String accessToken) throws Exception {
    return mockMvc.perform(accessToken != null ? request.header(AUTHORIZATION, accessToken) : request);
  }
}

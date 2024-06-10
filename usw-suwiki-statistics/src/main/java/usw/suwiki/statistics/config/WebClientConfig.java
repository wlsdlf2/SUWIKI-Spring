package usw.suwiki.statistics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import usw.suwiki.core.exception.BaseException;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static usw.suwiki.core.exception.ExceptionCode.EXTERNAL_API_FAILED;

@Configuration
public class WebClientConfig {
  
  @Bean
  public WebClient webClient() { // todo: (06.10) 유의미한 예외 던지기
    return WebClient.builder()
      .defaultHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .defaultStatusHandler(HttpStatusCode::is4xxClientError, response -> Mono.error(new BaseException(EXTERNAL_API_FAILED)))
      .defaultStatusHandler(HttpStatusCode::is5xxServerError, response -> Mono.error(new BaseException(EXTERNAL_API_FAILED)))
      .build();
  }
}

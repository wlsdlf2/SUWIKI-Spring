package usw.suwiki.auth.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import usw.suwiki.auth.core.interceptor.AuthorizationInterceptor;
import usw.suwiki.auth.core.resolver.AuthenticatedUserArgumentResolver;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
  private static final List<String> ALLOW_URL_LIST = List.of(
    "/docs/**", "/swagger-ui/**", "/swagger-resources/**", "/swagger-ui/**",
    "/v3/api-docs", "/index.html", "/swagger-ui.html", "/webjars/**"
  );

  private final AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;
  private final AuthorizationInterceptor authorizationInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authorizationInterceptor)
      .excludePathPatterns(ALLOW_URL_LIST)
      .addPathPatterns("/**");
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(authenticatedUserArgumentResolver);
  }
}

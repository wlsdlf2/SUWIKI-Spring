package usw.suwiki.infra.caffeine.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

@Configuration
public class ETagConfig {

  @Bean
  public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
    var filterFilterRegistrationBean = new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());

    filterFilterRegistrationBean.addUrlPatterns("/lecture/all");
    filterFilterRegistrationBean.setName("etagFilter");
    return new ShallowEtagHeaderFilter();
  }

}

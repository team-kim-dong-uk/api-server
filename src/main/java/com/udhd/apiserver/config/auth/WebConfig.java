package com.udhd.apiserver.config.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.udhd.apiserver.config.HTMLCharacterEscapes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${front.url}")
  private String frontUrl;

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOriginPatterns(frontUrl, "http://localhost:3000")
        .allowedMethods(
            HttpMethod.GET.name(),
            HttpMethod.HEAD.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.PATCH.name(),
            HttpMethod.DELETE.name()
        );
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(escapingConverter());
  }
  /*
  * 메세지 컨버터 추가
  * jackson을 사용해 json으로 변환될 때 HTMLCharacterEscapes에 명시된 문자열을 escapse해줍니다.
  * */
  @Bean
  public HttpMessageConverter<?> escapingConverter() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.getFactory().setCharacterEscapes(new HTMLCharacterEscapes());
    //objectMapper.getFactory().setCharacterEscapes(new HTMLCharacterEscapesTest());

    objectMapper.registerModule(new JavaTimeModule()); // 시간 데이터도 XSS 처리
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    MappingJackson2HttpMessageConverter htmlEscapingConverter = new MappingJackson2HttpMessageConverter();
    htmlEscapingConverter.setObjectMapper(objectMapper);

    return htmlEscapingConverter;
  }
}

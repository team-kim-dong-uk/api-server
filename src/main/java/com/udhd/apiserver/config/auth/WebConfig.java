package com.udhd.apiserver.config.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.http.HttpMethod;

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
                .allowedOriginPatterns(frontUrl)
                .allowedMethods(
                    HttpMethod.GET.name(),
    	            HttpMethod.HEAD.name(),
                	HttpMethod.POST.name(),
                	HttpMethod.PUT.name(),
                	HttpMethod.DELETE.name());
                );
    }
}

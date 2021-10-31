package com.udhd.apiserver.config;

import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfiguration {

  @Bean
  public MongoClientSettingsBuilderCustomizer mongoDBClientSettings() {
    return builder -> {
      builder.applyToSslSettings(blockBuilder -> {
        blockBuilder.enabled(true).invalidHostNameAllowed(true);
      });
    };
  }
}
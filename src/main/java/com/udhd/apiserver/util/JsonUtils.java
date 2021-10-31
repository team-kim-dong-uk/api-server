package com.udhd.apiserver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class JsonUtils {

  private static JsonUtils instance;
  ObjectMapper objectMapper;

  private JsonUtils() {
    objectMapper = new ObjectMapper();
  }

  public static JsonUtils getInstance() {
    if (instance == null) {
      instance = new JsonUtils();
    }
    return instance;
  }

  public String stringify(Map<String, Object> m) throws JsonProcessingException {
    return objectMapper.writeValueAsString(m);
  }

  public Map<String, Object> parse(String s) throws JsonProcessingException {
    return objectMapper.readValue(s, Map.class);
  }
}

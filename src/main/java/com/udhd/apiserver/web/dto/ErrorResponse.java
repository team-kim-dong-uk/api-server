package com.udhd.apiserver.web.dto;

import java.util.Collections;
import java.util.List;
import lombok.Getter;

@Getter
public class ErrorResponse implements GeneralResponse {

  int code;
  String message;
  List<String> errors;

  public ErrorResponse(int code, String message) {
    this.code = code;
    this.message = message;
    this.errors = Collections.emptyList();
  }

  public ErrorResponse(int code, String message, List<String> errors) {
    this.code = code;
    this.message = message;
    this.errors = errors;
  }

  public String toJson() {
    return "{ \"code\": \"" + code + "\"," +
        " \"message\": \"" + message + "\"," +
        " \"errors\": \"" + errors + "\"}";
  }

}

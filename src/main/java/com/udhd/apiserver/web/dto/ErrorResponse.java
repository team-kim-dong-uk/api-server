package com.udhd.apiserver.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class ErrorResponse {
    int status;
    String message;
    List<String> errors;

    public String toJson() {
        return "{ \"status\": \"" + status + "\"," +
                " \"message\": \"" + message + "\"," +
                " \"errors\": \"" + errors + "\"}";
    }

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.errors = Collections.emptyList();
    }

    public ErrorResponse(int status, String message, List<String> errors) {
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

}

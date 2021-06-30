package com.udhd.apiserver.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    int code;
    String message;

    public String toJson() {
        return "{ \"code\" : \"" + code + "\", \"message\" : \"" + message + "\" }";
    }

}

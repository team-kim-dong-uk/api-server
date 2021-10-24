package com.udhd.apiserver.web.dto;

import lombok.Data;

@Data
public class SuccessResponse implements GeneralResponse {
  String message;
}

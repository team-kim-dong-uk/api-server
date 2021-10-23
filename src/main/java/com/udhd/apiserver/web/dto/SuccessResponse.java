package com.udhd.apiserver.web.dto;

import lombok.Setter;

public class SuccessResponse implements GeneralResponse {
  @Setter
  String message;
}

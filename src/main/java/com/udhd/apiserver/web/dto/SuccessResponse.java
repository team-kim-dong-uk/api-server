package com.udhd.apiserver.web.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SuccessResponse implements GeneralResponse {

  String message;
}

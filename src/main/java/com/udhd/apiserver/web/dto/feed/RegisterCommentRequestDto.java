package com.udhd.apiserver.web.dto.feed;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterCommentRequestDto {

  private String content;
}

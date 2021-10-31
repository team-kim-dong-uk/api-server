package com.udhd.apiserver.web.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

  /**
   * update 할 닉네임
   */
  private String nickname;

  /**
   * update 할 그룹
   */
  private String group;
}

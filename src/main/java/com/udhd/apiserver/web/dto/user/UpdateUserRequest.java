package com.udhd.apiserver.web.dto.user;

import lombok.*;

@ToString
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

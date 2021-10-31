package com.udhd.apiserver.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@Document(collection = "user")
public class User {

  @Id
  private ObjectId id;
  private String email;
  private String nickname;
  private String group;
  private String refreshToken;
  private int uploadCount;
  private Integer likeCount = 0;
  private Integer saveCount = 0;

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public void addLike() {
    this.likeCount += 1;
  }

  public void addSave() {
    this.saveCount += 1;
  }

  public void deleteLike() {
    if (this.likeCount > 0) {
      this.likeCount -= 1;
    }
  }

  public void deleteSave() {
    if (this.saveCount > 0) {
      this.saveCount -= 1;
    }
  }
}

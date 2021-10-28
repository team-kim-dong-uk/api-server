package com.udhd.apiserver.domain.user;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@Document(collection = "user")
public class User implements Persistable<ObjectId> {

  @Id
  private ObjectId id;
  private String email;
  private String nickname;
  private String group;
  private String refreshToken;
  private int uploadCount;
  private Integer likeCount = 0;
  private Integer saveCount = 0;

  @CreatedDate
  private LocalDateTime createdDate;
  @LastModifiedDate
  private LocalDateTime modifiedDate;

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  @Override
  public boolean isNew() {
    return id == null;
  }

  public void addLike(){ this.likeCount += 1; }
  public void addSave(){ this.saveCount += 1; }
  public void deleteLike(){
    if(this.likeCount > 0)
      this.likeCount -= 1;
  }
  public void deleteSave(){
    if(this.saveCount > 0)
      this.saveCount -= 1;
  }
}

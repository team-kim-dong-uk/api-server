package com.udhd.apiserver.domain.feed;


import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Builder
@Data
public class Comment {
  @NotNull
  private ObjectId id; // subdocument 이기 때문에 직접 생성해서 넣어줘야함
  @NotNull
  private ObjectId userId;
  @NotNull
  private String userName;
  @CreatedDate
  private LocalDateTime createdDate; // unix timestamp
  @LastModifiedDate
  private LocalDateTime modifiedDate; // unix timestmap

  boolean deleted; // 만약 true이라면 반드시 다른 데이터들은 null임
  String content;
}

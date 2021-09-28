package com.udhd.apiserver.domain.upload;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "upload")
public class Upload {

  public static String STATUS_UPLOADING = "uploading";
  public static String STATUS_TAGGING = "tagging";
  public static String STATUS_REGISTERING = "registering";
  public static String STATUS_COMPLETED = "completed";
  public static String STATUS_ERROR = "error";
  @Id
  private ObjectId id;
  private ObjectId uploaderId;
  private String pollingKey;
  private String s3Url;
  private String checksum;
  private String status;  // TODO: enum 으로 변경하기
  private List<String> tags;
  private String fileId;  // for google drive

  @CreatedDate
  private LocalDateTime createdDate;

  @LastModifiedDate
  private LocalDateTime modifiedDate;
}

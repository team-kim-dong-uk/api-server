package com.udhd.apiserver.domain.upload;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "upload")
public class Upload {

    @Id
    private ObjectId id;
    private ObjectId uploaderId;
    private String pollingKey;
    private String s3Url;
    private String checksum;
    private String status;  // TODO: enum 으로 변경하기
    private List<String> tags;
    private String fileId;  // for google drive
}

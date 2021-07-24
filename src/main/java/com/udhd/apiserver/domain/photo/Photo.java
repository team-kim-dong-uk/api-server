package com.udhd.apiserver.domain.photo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Document(collection = "photo")
public class Photo {

    @Id
    private ObjectId id;
    private ObjectId uploaderId;
    private String thumbnailLink;
    private String originalLink;
    private String checksum;
    private List<String> tags;

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
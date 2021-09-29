package com.udhd.apiserver.domain.album;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Document(collection = "album")
public class Album {

    @Id
    private ObjectId id;
    private ObjectId userId;
    private ObjectId photoId;
    private String thumbnailLink;
    private Date lastViewed;
    private List<String> tags;


    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}

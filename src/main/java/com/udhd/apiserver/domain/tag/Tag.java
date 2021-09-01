package com.udhd.apiserver.domain.tag;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tag")
public class Tag {
    @Id
    private String tag;
}

package com.udhd.apiserver.domain.tag;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
  private int count;
}

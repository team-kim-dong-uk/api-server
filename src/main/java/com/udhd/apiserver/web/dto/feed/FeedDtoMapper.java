package com.udhd.apiserver.web.dto.feed;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import com.udhd.apiserver.domain.feed.Feed;

@Mapper(componentModel = "spring")
public interface FeedDtoMapper {
  default String map(ObjectId value) {
    return value.toString();
  }
  default ObjectId map(String value) {
    if (StringUtils.isEmpty(value) || !ObjectId.isValid(value))
      throw new IllegalArgumentException("cannot convert " + value + " to objectId");
    return new ObjectId(value);
  }
  default Long map(LocalDateTime localDateTime) {
    if (localDateTime == null)
      return null;
    return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }

  default LocalDateTime map(Long localDateTime) {
    if (localDateTime == null)
      return null;
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(localDateTime),
        TimeZone.getDefault().toZoneId());
  }
  FeedDto toDto(Feed feed);
  Feed toEntity(FeedDto feedDto);
}

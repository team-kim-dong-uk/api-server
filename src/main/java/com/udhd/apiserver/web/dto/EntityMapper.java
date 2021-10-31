package com.udhd.apiserver.web.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

public interface EntityMapper<D, E> {

  default String map(ObjectId value) {
    return value.toString();
  }

  default ObjectId map(String value) {
    if (StringUtils.isEmpty(value) || !ObjectId.isValid(value)) {
      throw new IllegalArgumentException("cannot convert " + value + " to objectId");
    }
    return new ObjectId(value);
  }

  default Long map(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return null;
    }
    return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }

  default LocalDateTime map(Long localDateTime) {
    if (localDateTime == null) {
      return null;
    }
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(localDateTime),
        TimeZone.getDefault().toZoneId());
  }

  E toEntity(D d);

  D toDto(E e);
}

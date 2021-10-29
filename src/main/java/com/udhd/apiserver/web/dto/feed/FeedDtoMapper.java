package com.udhd.apiserver.web.dto.feed;

import com.udhd.apiserver.web.dto.EntityMapper;
import org.mapstruct.Mapper;
import com.udhd.apiserver.domain.feed.Feed;

@Mapper(componentModel = "spring")
public interface FeedDtoMapper extends EntityMapper<FeedDto, Feed> {
  FeedDto toDto(Feed feed);
  Feed toEntity(FeedDto feedDto);
}

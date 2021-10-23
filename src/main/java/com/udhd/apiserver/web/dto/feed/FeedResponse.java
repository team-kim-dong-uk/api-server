package com.udhd.apiserver.web.dto.feed;

import com.udhd.apiserver.web.dto.GeneralResponse;
import java.util.List;
import lombok.Setter;

public class FeedResponse implements GeneralResponse {
  @Setter
  List<FeedDto> feeds;
}

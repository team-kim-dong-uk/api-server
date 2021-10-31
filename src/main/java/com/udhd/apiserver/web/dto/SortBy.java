package com.udhd.apiserver.web.dto;

import lombok.Getter;

@Getter
public enum SortBy {
  RANDOM,
  SAVED_AT,
  EVENT_DATE,
  LAST_VIEWED;
}

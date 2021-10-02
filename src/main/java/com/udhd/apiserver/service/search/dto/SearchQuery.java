package com.udhd.apiserver.service.search.dto;

import java.util.List;
import pics.udhd.kafka.dto.PhotoDto;

/**
 * TODO: 원래는 상속받아서 구현해야하는데, 일단 급하게 이렇게 구현. 상속받아서 똑바로 구현합시다
 *
 */
public interface SearchQuery {
  List<PhotoDto> getPhotoDtos();
}

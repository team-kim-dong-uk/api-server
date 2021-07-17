package com.udhd.apiserver.web.dto.photo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TagListDto {
    /**
     * 추천된 태그 목록
     */
    private List<String> tags;
}

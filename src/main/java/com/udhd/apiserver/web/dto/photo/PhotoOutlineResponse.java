package com.udhd.apiserver.web.dto.photo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PhotoOutlineResponse {
    private String photoId;
    private String smallLink;
}

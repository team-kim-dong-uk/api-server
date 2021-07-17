package com.udhd.apiserver.web.dto.album;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFavouriteRequest {
    /**
     * 즐겨찾기 지정 여부
     */
    @NotNull
    private Boolean favourite;
}

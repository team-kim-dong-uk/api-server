package com.udhd.apiserver.web.dto.album;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTagsRequest {
    /**
     * 새로운 태그들. 수정된 태그들만이 아닌 모든 태그를 전달해주어야 한다
     */
    @NotNull
    private List<String> tags;
}

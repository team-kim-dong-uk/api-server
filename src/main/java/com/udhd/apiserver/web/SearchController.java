package com.udhd.apiserver.web;

import com.udhd.apiserver.web.dto.photo.PhotoOutlineResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/users/{userId}/search")
@RestController
public class SearchController {
    private final List<PhotoOutlineResponse> mockSearchResults
            = Arrays.asList(PhotoOutlineResponse.builder()
                                    .photoId("456")
                                    .smallLink("http://link.com/456")
                                    .build());

    /**
     * userId에게 없는 사진들 중 해당 tags 들을 가진 사진들을 찾는다.
     *
     * @param userId   the user id
     * @param tags     검색할 태그 목록
     * @param sortBy   정렬기준
     * @param page     the page
     * @param pageSize the page size
     * @return the list
     */
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public List<PhotoOutlineResponse> searchTag(
            @PathVariable String userId,
            @RequestParam(defaultValue = "") List<String> tags,
            @RequestParam(defaultValue = "random") String sortBy,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "15") Integer pageSize) {
        return mockSearchResults;
    }

    /**
     * userId에게 없는 사진 중 photoId와 유사한 사진을 찾는다. TODO
     *
     * @param userId  the user id
     * @param photoId the photo id
     * @return the list
     */
    @GetMapping("/similar/{photoId}")
    @ResponseStatus(HttpStatus.OK)
    public List<PhotoOutlineResponse> searchSimilar(
            @PathVariable String userId,
            @PathVariable String photoId) {
        return mockSearchResults;
    }
}

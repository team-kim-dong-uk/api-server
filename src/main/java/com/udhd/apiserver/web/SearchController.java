package com.udhd.apiserver.web;

import com.udhd.apiserver.domain.tag.TagRepository;
import com.udhd.apiserver.service.AlbumService;
import com.udhd.apiserver.service.PhotoService;
import com.udhd.apiserver.service.SearchService;
import com.udhd.apiserver.util.SecurityUtils;
import com.udhd.apiserver.web.dto.photo.PhotoOutlineDto;
import com.udhd.apiserver.web.dto.search.SearchCandidateDto;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/{userId}/search")
@RestController
public class SearchController {
    private final PhotoService photoService;
    private final SearchService searchService;

    @GetMapping("/tags/recommended")
    public List<SearchCandidateDto> recommendedTags(@RequestParam String keyword) {
        return searchService.getRecommendedKeywords(keyword);
    }

    /**
     * userId에게 없는 사진들 중 해당 tags 들을 가진 사진들을 찾는다.
     *
     * @param userId   the user id
     * @param tags     검색할 태그 목록
     * @param sortBy   정렬기준
     * @param findAfter 지난번 검색결과의 가장 마지막 원소의 id. 이 원소 다음부터 찾기 시작한다. null이면 처음부터 찾는다.
     * @param fetchSize the fetch size
     * @return the list
     */
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public List<PhotoOutlineDto> searchTag(
            @PathVariable String userId,
            @RequestParam(defaultValue = "") List<String> tags,
            @RequestParam(defaultValue = "") String uploaderId,
            @RequestParam(defaultValue = "photoId") String sortBy,
            @RequestParam(required = false) String findAfter,
            @RequestParam(defaultValue = "21") Integer fetchSize) {
        SecurityUtils.checkUser(userId);

        List<PhotoOutlineDto> fetchedData = photoService.findPhotos(tags, uploaderId, findAfter, fetchSize);

        List<String> notDuplicatedPhotoIds = searchService.remainNotOwned(userId,
            fetchedData.stream().map(PhotoOutlineDto::getPhotoId).collect(Collectors.toList())
        );

        Set<String> notDuplicatedPhotoIdsSet = new HashSet<>(notDuplicatedPhotoIds);

        List<PhotoOutlineDto> retval = new ArrayList<>();
        for (PhotoOutlineDto photoOutlineDto : fetchedData) {
            if (notDuplicatedPhotoIdsSet.contains(photoOutlineDto.getPhotoId())) {
                retval.add(photoOutlineDto);
            }
        }

        return retval;
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
    public List<PhotoOutlineDto> searchSimilar(
            @PathVariable String userId,
            @PathVariable String photoId) {
        SecurityUtils.checkUser(userId);
        try {
            List<String> photoIds = searchService.remainNotOwned(userId, Arrays.asList(photoId));
            List<PhotoOutlineDto> retval = photoService.getPhotoDetailAll(photoIds);
            return retval;
        } catch (Exception e) {
            log.info("error", e);
            return Arrays.asList();
        }
    }
}

package com.udhd.apiserver.web;

import com.udhd.apiserver.web.dto.album.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/users/{userId}/album")
@RestController
public class AlbumController {
    private final AlbumDetailResponse mockAlbumDetailResponse
            = AlbumDetailResponse.builder()
                .albumId("456")
                .uploaderNickname("업로더")
                .originalLink("http://link.com/456")
                .favourite(true)
                .favouriteCount(532)
                .savedAt(new Date())
                .tags(Arrays.asList("더보이즈", "멤버1", "1집", "210701"))
                .build();

    /**
     * 앨범에 새 사진 저장. TODO
     *
     * @param userId               the user id
     * @param newAlbumRequest the new album photo request
     * @return the album photo detail response
     */
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public AlbumDetailResponse newAlbum(
            @PathVariable String userId,
            NewAlbumRequest newAlbumRequest) {
        return mockAlbumDetailResponse;
    }

    /**
     * 앨범 사진의 상세정보를 반환한다. TODO
     *
     * @param userId  the user id
     * @param albumId the album id
     * @return the album photo detail response
     */
    @GetMapping("/{albumId}")
    @ResponseStatus(HttpStatus.OK)
    public AlbumDetailResponse detailAlbum(
            @PathVariable String userId,
            @PathVariable String albumId) {
        return mockAlbumDetailResponse;
    }

    /**
     * 내 앨범 사진 리스트. TODO
     *
     * @param userId         the user id
     * @param tags           검색 태그 목록
     * @param sortBy         정렬 기준
     * @param uploadedOnly   true이면 내가 업로드한 사진만, false면 업로드/저장한 사진을 모두 보여줌
     * @param favouriteFirst 즐겨찾기 사진들을 먼저 보여줄지 여부
     * @param page           the page
     * @param pageSize       the page size
     * @return the list
     */
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public List<AlbumOutlineResponse> listAlbum(
            @PathVariable String userId,
            @RequestParam(defaultValue = "") List<String> tags,
            @RequestParam(defaultValue = "random") String sortBy,
            @RequestParam(defaultValue = "false") Boolean uploadedOnly,
            @RequestParam(defaultValue = "true") Boolean favouriteFirst,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "15") Integer pageSize) {
        return Arrays.asList(AlbumOutlineResponse.builder()
                                    .albumId("456")
                                    .smallLink("http://link.com/456")
                                    .build());
    }

    /**
     * 앨범 사진 정보를 업데이트한다.
     * request body에 업데이트할 필드만 넣어서 좋아요/태그수정 등의 요청을 보낼 수 있다.
     * 내가 업로드한 사진일 경우, 태그를 수정하면 다른 사용자들의 검색결과에 반영된다.
     * TODO
     *
     * @param userId                  the user id
     * @param albumId                 the album id
     * @param updateAlbumRequest the update album photo request
     * @return the album photo detail response
     */
    @PatchMapping("/{albumId}")
    @ResponseStatus(HttpStatus.OK)
    public AlbumDetailResponse updateAlbum(
            @PathVariable String userId,
            @PathVariable String albumId,
            @RequestBody UpdateAlbumRequest updateAlbumRequest) {
        return mockAlbumDetailResponse;
    }

    /**
     * 앨범 사진을 삭제한다.
     * 내가 업로드한 사진일 경우, 더이상 다른 사용자들이 검색할 수 없다.
     * TODO
     *
     * @param userId  the user id
     * @param albumId the album id
     */
    @DeleteMapping("/{albumId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAlbum(@PathVariable String userId, @PathVariable String albumId) {
        return;
    }
}

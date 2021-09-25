package com.udhd.apiserver.web.dto.search;

import com.udhd.apiserver.domain.tag.Tag;
import com.udhd.apiserver.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchCandidateDto {
    /**
     * type: TAG | USER
     */
    private String type;
    /**
     * 검색어가 될 단어
     */
    private String keyword;
    /**
     * 해당 검색어를 가지는 사진 수
     */
    private int count;
    /**
     * type == USER 인 경우, 해당 유저의 userId
     */
    private String userId;

    public static SearchCandidateDto fromTag(Tag tag) {
        return new SearchCandidateDto("TAG", tag.getTag(), tag.getCount(), null);
    }

    public static SearchCandidateDto fromUser(User user) {
        return new SearchCandidateDto("USER", user.getNickname(), user.getUploadCount(), user.getId().toString());
    }
}

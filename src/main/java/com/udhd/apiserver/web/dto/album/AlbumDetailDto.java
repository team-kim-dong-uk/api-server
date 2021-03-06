package com.udhd.apiserver.web.dto.album;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDetailDto {

  /**
   * 저장한 앨범의 id
   */
  private String albumId;
  /**
   * 저장한 사진의 id
   */
  private String photoId;
  /**
   * 사진 업로더 id
   */
  private String uploaderId;
  /**
   * 사진 업로더 닉네임
   */
  private String uploaderNickname;
  /**
   * 원본사진 링크
   */
  private String originalLink;
  /**
   * 저장 날짜
   */
  private Date savedAt;
  /**
   * 태그
   */
  private List<String> tags;
}

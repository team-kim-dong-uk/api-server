package com.udhd.apiserver.service;

import com.udhd.apiserver.domain.album.Album;
import com.udhd.apiserver.domain.feed.Feed;
import com.udhd.apiserver.domain.feed.FeedRepository;
import com.udhd.apiserver.domain.photo.Photo;
import com.udhd.apiserver.domain.user.User;
import com.udhd.apiserver.exception.auth.DuplicateNicknameException;
import com.udhd.apiserver.exception.auth.InvalidAccessTokenException;
import com.udhd.apiserver.exception.user.UserNotFoundException;
import com.udhd.apiserver.service.feed.FeedException;
import com.udhd.apiserver.service.feed.FeedService;
import com.udhd.apiserver.service.search.SearchService;
import com.udhd.apiserver.web.dto.user.UpdateUserRequest;
import com.udhd.apiserver.web.dto.user.UserDto;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class FeedServiceTest {
    @InjectMocks
    FeedService feedService;
    @Mock
    private FeedRepository feedRepository;
    @Mock
    private UserService userService;
    @Mock
    private AlbumService albumService;
    @Mock
    private SearchService searchService;
    @Mock
    private MongoTemplate mongoTemplate;

    private static String userId = "6110066323a94f7c27f9cf4c";
    private static String feedId = "6110066323a94f7c27f9cf4d";
    private static String feedId_a = "6110066323a94f7c27f9cf4e";
    private static String photoId = "6110066323a94f7c27f9cf4f";
    private static String photoId_a = "6110066323a94f7c27f9cf41";
    private static String commentId = "6110066323a94f7c27f9cf42";

    User user = User.builder()
            .id(new ObjectId(userId))
            .nickname("tester")
            .group("omygirl")
            .likeCount(3)
            .saveCount(2)
            .build();
    private Photo photo = Photo.builder()
            .id(new ObjectId(photoId))
            .uploaderId(new ObjectId(userId))
            .thumbnailLink("thumbnail.com")
            .scaledLink("scaled.com")
            .originalLink("origin.com")
            .checksum("123123123123")
            .tags(List.of("오마이걸"))
            .hash(Integer.toString(photoId.hashCode()))
            .build();
    private Photo photo_a = Photo.builder()
            .id(new ObjectId(photoId_a))
            .uploaderId(new ObjectId(userId))
            .thumbnailLink("thumbnail_a.com")
            .scaledLink("scaled_a.com")
            .originalLink("origin_a.com")
            .checksum("456456456456")
            .tags(List.of("오마이걸"))
            .hash(Integer.toString(photoId_a.hashCode()))
            .build();
    private final Feed feed = Feed.builder()
            .id(new ObjectId(feedId))
            .photo(photo)
            .comments(new ArrayList<>())
            .likes(new ArrayList<>())
            .build();
    private Feed feed_a = Feed.builder()
            .id(new ObjectId(feedId_a))
            .photo(photo_a)
            .comments(new ArrayList<>())
            .likes(new ArrayList<>())
            .build();
    private List<Feed> feeds = List.of(feed, feed_a);
    private Album album = Album.builder()
            .feedId(new ObjectId(feedId))
            .build();


    @Test
    @DisplayName("Feed 가져오기")
    void getFeeds() throws FeedException {
        given(feedRepository.findAllByCreatedTimestampAfterOrderByOrder(any()))
                .willReturn(feeds);
        assertThat(feedService.getFeeds(userId)).isEqualTo(feeds);
    }
}


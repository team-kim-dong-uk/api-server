package com.udhd.apiserver.service.search;

import com.udhd.apiserver.service.search.dto.TaggedPhotoDto;
import com.udhd.apiserver.util.bktree.BkTreeSearcher;
import com.udhd.apiserver.util.bktree.BkTreeSearcher.Match;
import com.udhd.apiserver.util.bktree.Metric;
import com.udhd.apiserver.util.bktree.MutableBkTree;
import com.udhd.apiserver.util.bktree.SearchOption;
import com.udhd.apiserver.util.bktree.SearchResult;
import dev.brachtendorf.jimagehash.hash.Hash;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersistentPhotoBkTreeService {

  private static final int defaultDistance = 5;
  private final TaggedPhotoService taggedPhotoService;
  MutableBkTree<TaggedPhotoDto> bktree;
  Metric<TaggedPhotoDto> hammingDistance;
  BkTreeSearcher<TaggedPhotoDto> searcher;
  @Getter
  private int size;

  public void clear() {
    initialize();
  }

  @PostConstruct
  public void reset() {
    initialize();
    if (taggedPhotoService == null) {
      return;
    }
    List<TaggedPhotoDto> photos = taggedPhotoService.findAll();
    for (TaggedPhotoDto photo : photos) {
      if (photo != null && photo.getHash() != null)
        bktree.add(photo);
    }
  }

  private void initialize() {
    size = 0;
    hammingDistance = new TaggedPhotoMatric();
    bktree = new MutableBkTree<>(hammingDistance);
    searcher = new BkTreeSearcher<>(bktree);
  }

  public void insert(TaggedPhotoDto taggedPhoto) {
    // TODO: when duplicated photoId, this method throws Exception. It must be tracked by log.
    taggedPhotoService.save(taggedPhoto);
    bktree.add(taggedPhoto);
    size++;
  }

  public void insertAll(TaggedPhotoDto[] taggedPhotos) {
    taggedPhotoService.saveAll(taggedPhotos);
    bktree.addAll(taggedPhotos);
    size += taggedPhotos.length;
  }

  public List<? extends TaggedPhotoDto> search(TaggedPhotoDto taggedPhoto) {
    return this.search(taggedPhoto, defaultDistance);
  }

  public List<? extends TaggedPhotoDto> search(TaggedPhotoDto taggedPhoto, int distance) {
    return this.search(taggedPhoto, distance, Integer.MAX_VALUE, 0);
  }

  public List<? extends TaggedPhotoDto> search(TaggedPhotoDto taggedPhoto, int maxDistance,
      int limit) {
    return this.search(taggedPhoto, maxDistance, limit, Integer.MIN_VALUE);
  }

  public List<? extends TaggedPhotoDto> search(TaggedPhotoDto taggedPhoto, int maxDistance,
      int limit,
      int minDistance) {
    log.info("searcher : " + searcher.toString());
    if (taggedPhoto == null || taggedPhoto.getPhotoId() == null ||  taggedPhoto.getHash() == null)
      return Collections.emptyList();
    log.info("taggedPhoto : " + taggedPhoto.toString());
    SearchOption searchOption = SearchOption.builder()
        .minDistance(minDistance)
        .maxDistance(maxDistance)
        .limit(limit)
        .build();
    SearchResult<? extends TaggedPhotoDto> result = searcher.search(taggedPhoto, searchOption);
    List<TaggedPhotoDto> retval = new ArrayList<>();
    List<Match<? extends TaggedPhotoDto>> matches = new ArrayList<>(result.getMatches());
    Collections.shuffle(matches);
    matches.sort((o1, o2) -> {
      int d1 = o1.getDistance();
      int d2 = o2.getDistance();
      if (d1 < d2)
        return -1;
      else if (d1 > d2)
        return 1;
      return 0;
    });

    for (Match<? extends TaggedPhotoDto> element : matches) {
      retval.add(element.getMatch());
    }
    return retval;
  }

  public List<? extends TaggedPhotoDto> search(Hash hash) {
    return this.search(hash, defaultDistance);
  }

  public List<? extends TaggedPhotoDto> search(Hash hash, int distance) {
    TaggedPhotoDto tmpImage = TaggedPhotoDto.builder()
        .hash(hash)
        .build();
    return this.search(tmpImage, distance);
  }
}
class TaggedPhotoMatric implements Metric<TaggedPhotoDto> {

  @Override
  public int distance(TaggedPhotoDto x, TaggedPhotoDto y) {
    try {
      Hash xHash = x.getHash();
      Hash yHash = y.getHash();
      return xHash.hammingDistance(yHash);
    } catch (Exception e) {
      // TODO: Exception handling
      return Integer.MIN_VALUE;
    }
  }
}

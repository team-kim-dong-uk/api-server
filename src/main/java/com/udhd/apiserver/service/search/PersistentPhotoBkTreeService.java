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
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersistentPhotoBkTreeService implements PhotoBkTreeService {

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
    bktree.addAll(photos);
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
    return this.search(taggedPhoto, Integer.MIN_VALUE, distance, Integer.MAX_VALUE);
  }

  @Override
  public List<? extends TaggedPhotoDto> search(TaggedPhotoDto taggedPhoto, int maxDistance,
      int limit) {
    return this.search(taggedPhoto, maxDistance, limit, Integer.MIN_VALUE);
  }

  @Override
  public List<? extends TaggedPhotoDto> search(TaggedPhotoDto taggedPhoto, int maxDistance,
      int limit,
      int minDistance) {
    log.info("searcher : " + searcher.toString());
    log.info("taggedPhoto : " + taggedPhoto.toString());
    SearchOption searchOption = SearchOption.builder()
        .minDistance(minDistance)
        .maxDistance(maxDistance)
        .limit(limit)
        .build();
    SearchResult<? extends TaggedPhotoDto> result = searcher.search(taggedPhoto, searchOption);
    List<TaggedPhotoDto> retval = new ArrayList<>();
    for (Match<? extends TaggedPhotoDto> element : result.getMatches()) {
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

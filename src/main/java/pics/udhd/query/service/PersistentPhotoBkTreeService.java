package pics.udhd.query.service;

import dev.brachtendorf.jimagehash.hash.Hash;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.udhd.apiserver.domain.taggedphoto.TaggedPhotoRepository;
import com.udhd.apiserver.domain.taggedphoto.TaggedPhotoVO;
import pics.udhd.query.service.dto.TaggedPhoto;
import pics.udhd.query.util.bktree.BkTreeSearcher;
import pics.udhd.query.util.bktree.BkTreeSearcher.Match;
import pics.udhd.query.util.bktree.Metric;
import pics.udhd.query.util.bktree.MutableBkTree;
import pics.udhd.query.util.bktree.SearchOption;
import pics.udhd.query.util.bktree.SearchResult;

@Slf4j
@Service
public class PersistentPhotoBkTreeService implements PhotoBkTreeService {
  MutableBkTree<TaggedPhoto> bktree;
  Metric<TaggedPhoto> hammingDistance;
  BkTreeSearcher<TaggedPhoto> searcher;
  private static final int defaultDistance = 5;

  TaggedPhotoRepository taggedPhotoRepository;

  HashService hashService;

  TaggedPhotoService taggedPhotoService;

  @Getter
  private int size;
  @Autowired
  public PersistentPhotoBkTreeService(TaggedPhotoRepository taggedPhotoRepository,
      HashService hashService,
      TaggedPhotoService taggedPhotoService) {
    this.taggedPhotoRepository = taggedPhotoRepository;
    this.taggedPhotoService = taggedPhotoService;
    this.hashService = hashService;
    reset();
  }

  public void clear() {
    initialize();
  }

  public void reset() {
    initialize();
    List<TaggedPhotoVO> photos = taggedPhotoRepository.findAll();
    List<TaggedPhoto> data = photos.stream().map(photo -> TaggedPhoto.builder()
        .photoId(photo.getPhotoId().toString())
        .hash(hashService.generateHash(photo.getHash()))
        .build()).collect(Collectors.toList());
    bktree.addAll(data);
  }

  private void initialize() {
    size = 0;
    hammingDistance = new TaggedPhotoMatric();
    bktree = new MutableBkTree<>(hammingDistance);
    searcher = new BkTreeSearcher<>(bktree);
  }

  public void insert(TaggedPhoto taggedPhoto) {
    // TODO: when duplicated photoId, this method throws Exception. It must be tracked by log.
    taggedPhotoService.save(taggedPhoto);
    bktree.add(taggedPhoto);
    size ++;
  }

  public void insertAll(TaggedPhoto[] taggedPhotos) {
    taggedPhotoService.saveAll(taggedPhotos);
    bktree.addAll(taggedPhotos);
    size += taggedPhotos.length;
  }

  public List<? extends TaggedPhoto> search(TaggedPhoto taggedPhoto) {
    return this.search(taggedPhoto, defaultDistance);
  }

  public List<? extends TaggedPhoto> search(TaggedPhoto taggedPhoto, int distance) {
    log.info("searcher : " + searcher.toString());
    log.info("taggedPhoto : " + taggedPhoto.toString());
    SearchOption searchOption = SearchOption.builder().maxDistance(distance).build();
    SearchResult<? extends TaggedPhoto> result = searcher.search(taggedPhoto, searchOption);
    List<TaggedPhoto> retval = new ArrayList<>();
    for (Match<? extends TaggedPhoto> element : result.getMatches()) {
      retval.add(element.getMatch());
    }
    return retval;
  }

  @Override
  public List<? extends TaggedPhoto> search(TaggedPhoto taggedPhoto, int maxDistance, int limit) {
    return null;
  }

  @Override
  public List<? extends TaggedPhoto> search(TaggedPhoto taggedPhoto, int maxDistance, int limit,
      int minDistance) {
    return null;
  }

  public List<? extends TaggedPhoto> search(Hash hash) {
    return this.search(hash, defaultDistance);
  }
  public List<? extends TaggedPhoto> search(Hash hash, int distance) {
    TaggedPhoto tmpImage = TaggedPhoto.builder()
        .hash(hash)
        .build();
    return this.search(tmpImage, distance);
  }
}

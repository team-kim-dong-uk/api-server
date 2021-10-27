package pics.udhd.query.service;

import dev.brachtendorf.jimagehash.hash.Hash;
import java.util.List;
import pics.udhd.query.service.dto.TaggedPhoto;
import pics.udhd.query.util.bktree.Metric;

public interface PhotoBkTreeService {
  void clear();
  void reset();
  int getSize();
  void insert(TaggedPhoto taggedPhoto);
  List<? extends TaggedPhoto> search(TaggedPhoto taggedPhoto);
  List<? extends TaggedPhoto> search(TaggedPhoto taggedPhoto, int maxDistance);
  List<? extends TaggedPhoto> search(TaggedPhoto taggedPhoto, int maxDistance, int limit);
  List<? extends TaggedPhoto> search(TaggedPhoto taggedPhoto, int maxDistance, int limit, int minDistance);
  List<? extends TaggedPhoto> search(Hash hash);
  List<? extends TaggedPhoto> search(Hash hash, int distance);
}

class TaggedPhotoMatric implements Metric<TaggedPhoto> {
  @Override
  public int distance(TaggedPhoto x, TaggedPhoto y) {
    try {
      Hash xHash = x.getHash();
      Hash yHash = y.getHash();
      return xHash.hammingDistance(yHash);
    } catch (Exception e) {
      // TODO: Exception handling
      return -1;
    }
  }
}
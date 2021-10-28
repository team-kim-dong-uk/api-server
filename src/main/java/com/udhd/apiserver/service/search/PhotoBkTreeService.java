package com.udhd.apiserver.service.search;

import dev.brachtendorf.jimagehash.hash.Hash;
import java.util.List;
import com.udhd.apiserver.util.bktree.Metric;

public interface PhotoBkTreeService {
  void clear();
  void reset();
  int getSize();
  void insert(TaggedPhotoDto taggedPhoto);
  List<? extends TaggedPhotoDto> search(TaggedPhotoDto taggedPhoto);
  List<? extends TaggedPhotoDto> search(TaggedPhotoDto taggedPhoto, int maxDistance);
  List<? extends TaggedPhotoDto> search(TaggedPhotoDto taggedPhoto, int maxDistance, int limit);
  List<? extends TaggedPhotoDto> search(TaggedPhotoDto taggedPhoto, int maxDistance, int limit, int minDistance);
  List<? extends TaggedPhotoDto> search(Hash hash);
  List<? extends TaggedPhotoDto> search(Hash hash, int distance);
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
      return -1;
    }
  }
}
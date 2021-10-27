package pics.udhd.query.service.dto;

import dev.brachtendorf.jimagehash.hash.Hash;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaggedPhoto {
  String photoId;
  Hash hash;
  String url;

  @Override
  public String toString() {
    return "TaggedPhoto(photoId : " + photoId + ", hash :" + hash.toString() + ", url : " + url;
  }
}

package pics.udhd.query.service;

import dev.brachtendorf.jimagehash.hash.Hash;
import java.awt.image.BufferedImage;

public interface HashService {
  Hash generateHash(String hashString);
  Hash generateHash(BufferedImage image);
  String convertToString(Hash hash);
}

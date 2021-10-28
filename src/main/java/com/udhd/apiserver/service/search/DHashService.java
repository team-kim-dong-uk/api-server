package com.udhd.apiserver.service.search;

import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.DifferenceHash;
import dev.brachtendorf.jimagehash.hashAlgorithms.DifferenceHash.Precision;
import dev.brachtendorf.jimagehash.hashAlgorithms.HashingAlgorithm;
import java.awt.image.BufferedImage;
import java.math.BigInteger;
import org.springframework.stereotype.Service;

@Service
public class DHashService implements HashService {
  HashingAlgorithm hashingAlgorithm;
  int bitResolution;
  Precision precision;

  public DHashService() {
    this(32, Precision.Simple);
  }
  public DHashService(int bitResolution, Precision precision) {
    super();
    hashingAlgorithm = new DifferenceHash(bitResolution, precision);
  }

  public Hash generateHash(BufferedImage image) {
    return hashingAlgorithm.hash(image);
  }

  /*
   TODO: DHash is encapsulated. Originally, cannot access into algorithmId and hashLength.
   So you have to di.
   */
  public Hash generateHash(String hashString) {
    final int algorithmId = 771332648;
    final int hashLength = 35;
    return new Hash(new BigInteger(hashString, 16), hashLength, algorithmId);
  }

  public String convertToString(Hash hash) {
    return hash.getHashValue().toString(16);
  }
}

package com.udhd.apiserver.service;

import com.udhd.apiserver.domain.upload.Upload;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pics.udhd.kafka.TagCommander;

@RequiredArgsConstructor
@Service
public class TagService {
  private final TagCommander tagCommander;

  // TODO: Unimplmented
  public List<String> fetchTag(Upload upload) {
    return Arrays.asList("오마이걸", "1집");
  }
}

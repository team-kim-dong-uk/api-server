package pics.udhd.kafka;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TagCommander {
  List<String> mockTags = Arrays.asList("오마이걸", "1집");
  public List<String> fetchRecommendationalTags() {
    return mockTags;
  }
}

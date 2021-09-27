package pics.udhd.kafka;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pics.udhd.kafka.dto.PhotoDto;
import pics.udhd.kafka.dto.QueryResultDto;
import pics.udhd.kafka.dto.internal.InsertDto;
import pics.udhd.kafka.dto.internal.QueryDto;
import pics.udhd.kafka.dto.internal.QueryState;
import pics.udhd.kafka.dto.internal.ResultDto;


/**
 * ----------------------------               ----------------------------- |
 * QueryExecutor(Responsor) |  - (kafka) -  | QueryCommander(Requestor) |
 * ----------------------------               -----------------------------
 *
 * [en] QueryCommander is a service layer for kafka. It send a request through TOPIC_RES, TOPIC_INS
 * and receive through TOPIC_RES. All strings of topics must be placed on the project's
 * configuration file. (like application.properties)
 *
 * Usage : Add @Service Annotation and injected using @Autowired on the conroller layer.
 *
 * In principle, it does not require @Service annotation. But it has this for simple implementation.
 * When the complexity of project increase, it will be seperated.
 *
 * Internal comments are written by a form of debug log.
 *
 * [ko] QuerCommander는 kafka의 service layer로서, TOPIC_RES, TOPIC_INS로 요청을 보내고, TOPIC_RES를 통해서 응답을
 * 받는다. topic들에 대한 설정을 application.properties와 같은 파일에서 해주어야 한다.
 *
 * 사용법 : @Serivce 어노테이션을 추가하고 Controller Layer에서 @Autowired해서 사용.
 *
 * 원칙적으로는 Contoller에 해당하기에 @Service 가 불필요해야하자만, 단순한 구현을 하기 위해서 Service의 기능을 사용한다. 만약 복잡성이 증가한다면
 * 설계적으로 이를 분리시키는 작업을 해주어야한다.
 *
 * @author Min-Uk.Lee (makerdark98@gmail.com)
 * @see QueryExecutor
 */
@Service
@Slf4j
public class QueryCommander {

  private final KafkaTemplate<String, QueryDto> queryTemplate;
  private final KafkaTemplate<String, InsertDto> insertTemplate;
  // TODO: timeoutdelay 도 주입받도록 해야함.
  private final int timeoutDelay = 5000;
  Map<UUID, QueryState> progress;
  @Value("${spring.kafka.template.req-topic}")
  private String TOPIC_REQ;
  @Value("${spring.kafka.template.res-topic}")
  private String TOPIC_RES;
  @Value("${spring.kafka.template.ins-topic}")
  private String TOPIC_INS;
  private final Timer timer;

  @Autowired
  public QueryCommander(
      KafkaTemplate<String, QueryDto> _queryTemplate,
      KafkaTemplate<String, InsertDto> _insertTemplate) {
    this.queryTemplate = _queryTemplate;
    this.insertTemplate = _insertTemplate;
    progress = new HashMap<UUID, QueryState>();
    this.timer = new Timer(true);
  }

  public QueryResultDto search(List<PhotoDto> query) {
    Object syncObject = new Object();
    /**
     * callback 에서 값을 받기 위해서 만듬
     */
    final QueryResultDto[] result = new QueryResultDto[1];
    try {
      search(query, new QueryCallback() {
        @Override
        public void execute(QueryResultDto _result) {
          result[0] = _result;
          synchronized (syncObject) {
            syncObject.notify();
          }
        }

        @Override
        public void fail(QueryResultDto _result) {
          result[0] = _result;
          synchronized (syncObject) {
            syncObject.notify();
          }
        }
      });
    } catch (Exception e) {
      log.info("kafka fail");
      return null;
    }
    try {
      synchronized (syncObject) {
        syncObject.wait(2 * timeoutDelay);
      }
    } catch (InterruptedException e) {
      return null;
    }
    return result[0];
  }

  public void search(List<PhotoDto> query, QueryCallback callback) {
    QueryState state = new QueryState();
    state.queryKey = UUID.randomUUID();
    state.callback = callback;
    state.result = new QueryResultDto();
    state.query = query;
    progress.put(state.queryKey, state);
    TimerTask timeout = new TimerTask() {
      public void run() {
        release(state.queryKey);
      }
    };
    state.query.stream().forEach(photoDto -> {
      sendQuery(generateQuery(state, photoDto));
    });
    timer.schedule(timeout, timeoutDelay);
  }

  protected QueryDto generateQuery(QueryState state, PhotoDto photoDto) {
    return new QueryDto(state.queryKey, photoDto);
  }

  protected void sendQuery(QueryDto queryDto) {
    queryTemplate.send(TOPIC_REQ, queryDto);
  }

  public void insert(PhotoDto photoDto) {
    sendInsert(generateInsertDto(photoDto));
  }

  public void insertAll(Collection<PhotoDto> photoDtos) {
    photoDtos.stream().forEach(photoDto -> sendInsert(generateInsertDto(photoDto)));
  }

  protected InsertDto generateInsertDto(PhotoDto photoDto) {
    return new InsertDto(photoDto);
  }

  protected void sendInsert(InsertDto insertDto) {
    insertTemplate.send(TOPIC_INS, insertDto);
  }

  public void release(UUID queryKey) {
    if (queryKey == null) {
      // TODO: Error Logging
      return;
    }
    if (progress.containsKey(queryKey)) {
      progress.remove(queryKey);
    } else {
      // TODO: Logging duplicated remove operation.
      return;
    }
  }

  public void reply(QueryState state) {
    state.callback.execute(state.result);
  }

  /**
   * [en] [ko] TODO: In this method, nullpointer Exception occurs.
   */
  @KafkaListener(
      topics = "${spring.kafka.template.res-topic}",
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void consume(ResultDto resultDto) {
    QueryState queryState = progress.get(resultDto.getQueryKey());
    if (queryState == null) {
      log.info("cannot find querykey : " + resultDto.getQueryKey() +
          ", photoId : " + resultDto.getPhotoId());
      return;
    }
    queryState.accumulateResult(resultDto);

    if (queryState.isDone()) {
      reply(queryState);
      release(resultDto.getQueryKey());
    }
  }

}

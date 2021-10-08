package com.udhd.apiserver.domain.upload;

import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UploadRepository extends MongoRepository<Upload, ObjectId> {

  Upload save(Upload upload);

  @Override
  <S extends Upload> List<S> saveAll(Iterable<S> entities);

  Long countByPollingKey(String pollingKey);

  Long countByPollingKeyAndStatus(String pollingKey, String status);

  Upload findByPollingKeyAndChecksum(String pollingKey, String checksum);
}

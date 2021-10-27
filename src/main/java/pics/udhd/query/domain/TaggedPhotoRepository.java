package pics.udhd.query.domain;

import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TaggedPhotoRepository extends MongoRepository<TaggedPhotoVO, ObjectId> {
  Optional<TaggedPhotoVO> findById(ObjectId id);
}

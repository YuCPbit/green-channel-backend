package edu.greenchannel.tutorapply;

import java.util.List;
import java.util.Optional;

public interface TutorApplyTypeRepository {
    List<TutorApplyType> findAll(boolean enabledOnly);

    Optional<TutorApplyType> findById(long id);

    boolean existsByCode(String code, Long excludedId);

    TutorApplyType insert(TutorApplyType type);

    TutorApplyType update(TutorApplyType type);

    void softDelete(long id);
}

package edu.greenchannel.tutor.repository;

import edu.greenchannel.tutor.entity.TutorApplyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 申请类型配置 Repository
 */
public interface TutorApplyTypeRepository extends JpaRepository<TutorApplyType, Long> {

    List<TutorApplyType> findByStatusAndIsDeletedOrderBySortAsc(Integer status, Integer isDeleted);

    @Query("SELECT t FROM TutorApplyType t WHERE t.status = 1 AND t.isDeleted = 0 ORDER BY t.sort ASC")
    List<TutorApplyType> findAllActive();
}

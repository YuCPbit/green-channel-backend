package edu.greenchannel.tutor.repository;

import edu.greenchannel.tutor.entity.TutorAppReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 审核记录 Repository
 */
public interface TutorAppReviewRepository extends JpaRepository<TutorAppReview, Long> {

    List<TutorAppReview> findByApplicationIdAndIsDeletedOrderByReviewTimeAsc(Long applicationId, Integer isDeleted);
}

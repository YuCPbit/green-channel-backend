package edu.greenchannel.subsidy.repository;

import edu.greenchannel.subsidy.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CollegeRepository extends JpaRepository<College, Long> {

    List<College> findByIsDeletedOrderByCollegeNameAsc(Integer isDeleted);

    @Query(value = "SELECT DISTINCT grade FROM gc_class WHERE is_deleted = 0 ORDER BY grade DESC", nativeQuery = true)
    List<Integer> findDistinctGrades();
}

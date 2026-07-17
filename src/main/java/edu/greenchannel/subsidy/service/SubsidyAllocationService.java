package edu.greenchannel.subsidy.service;

import edu.greenchannel.subsidy.dto.request.AllocationCreateRequest;
import edu.greenchannel.subsidy.dto.response.AllocationItemResponse;
import edu.greenchannel.subsidy.dto.response.AllocationSummaryResponse;
import edu.greenchannel.subsidy.entity.College;

import java.util.List;

public interface SubsidyAllocationService {
    void allocateQuota(AllocationCreateRequest request, Long currentUserId, Integer currentUserRole, Long currentUserCollegeId);
    AllocationSummaryResponse getSummary(Long batchId, Integer currentUserRole, Long currentUserCollegeId);
    List<AllocationItemResponse> listAllocations(Long batchId, Integer targetType);
    List<College> listColleges();
    List<Integer> listGrades();
}

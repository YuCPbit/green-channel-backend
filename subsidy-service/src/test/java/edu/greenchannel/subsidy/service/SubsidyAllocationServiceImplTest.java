package edu.greenchannel.subsidy.service;

import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.subsidy.dto.request.AllocationCreateRequest;
import edu.greenchannel.subsidy.repository.CollegeRepository;
import edu.greenchannel.subsidy.repository.SubsidyAllocationRepository;
import edu.greenchannel.subsidy.repository.SubsidyApplyRepository;
import edu.greenchannel.subsidy.repository.SubsidyBatchRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubsidyAllocationServiceImplTest {

    private final SubsidyAllocationRepository allocationRepository = mock(SubsidyAllocationRepository.class);
    private final SubsidyBatchRepository batchRepository = mock(SubsidyBatchRepository.class);
    private final CollegeRepository collegeRepository = mock(CollegeRepository.class);
    private final SubsidyApplyRepository applyRepository = mock(SubsidyApplyRepository.class);
    private final SubsidyAllocationServiceImpl service = new SubsidyAllocationServiceImpl(
            allocationRepository, batchRepository, collegeRepository, applyRepository);

    @Test
    void studentCannotSpoofSchoolRoleToAllocateQuota() {
        AllocationCreateRequest request = new AllocationCreateRequest(1L, 1, 10L, new BigDecimal("1000"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.allocateQuota(request, user(1, 7L)));

        assertEquals(40300, exception.getCode());
    }

    @Test
    void collegeAdminMustHaveACollegeAssignment() {
        when(applyRepository.findCollegeIdByUserId(9L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.getSummary(1L, user(3, 9L)));

        assertEquals(40400, exception.getCode());
    }

    private CurrentUser user(int userType, long id) {
        return new CurrentUser(id, "tester", "测试用户", userType, "角色",
                List.of(), List.of(), List.of());
    }
}

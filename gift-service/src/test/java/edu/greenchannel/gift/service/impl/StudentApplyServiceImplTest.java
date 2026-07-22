package edu.greenchannel.gift.service.impl;

import edu.greenchannel.common.BusinessException;
import edu.greenchannel.gift.entity.StudentApply;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StudentApplyServiceImplTest {

    @Test
    void pickupMarksPendingApplicationAsCompleted() {
        StudentApply apply = application(StudentApply.PICKUP_PENDING);
        FakeStudentApplyService service = new FakeStudentApplyService(apply, true);

        StudentApply result = service.pickup(" CODE-001 ", 88L, "现场领取");

        assertEquals(StudentApply.PICKUP_COMPLETED, result.getPickupStatus());
        assertEquals(88L, result.getPickupOperatorId());
        assertEquals("现场领取", result.getPickupRemark());
        assertNotNull(result.getPickupTime());
        assertEquals("CODE-001", service.requestedCode);
    }

    @Test
    void pickupRejectsRepeatedOperation() {
        FakeStudentApplyService service = new FakeStudentApplyService(
                application(StudentApply.PICKUP_COMPLETED), true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.pickup("CODE-001", 88L, null));

        assertEquals(40900, exception.getCode());
    }

    @Test
    void pickupRejectsUnknownCode() {
        FakeStudentApplyService service = new FakeStudentApplyService(null, true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.pickup("UNKNOWN", 88L, null));

        assertEquals(40400, exception.getCode());
    }

    private static StudentApply application(int pickupStatus) {
        StudentApply apply = new StudentApply();
        apply.setId(1L);
        apply.setPickupCode("CODE-001");
        apply.setPickupStatus(pickupStatus);
        return apply;
    }

    private static final class FakeStudentApplyService extends StudentApplyServiceImpl {
        private final StudentApply apply;
        private final boolean updateResult;
        private String requestedCode;

        private FakeStudentApplyService(StudentApply apply, boolean updateResult) {
            this.apply = apply;
            this.updateResult = updateResult;
        }

        @Override
        protected StudentApply findByPickupCode(String pickupCode) {
            requestedCode = pickupCode;
            return apply;
        }

        @Override
        protected boolean markPickedUp(Long applyId, Long operatorId, String remark, LocalDateTime pickupTime) {
            return updateResult;
        }
    }
}

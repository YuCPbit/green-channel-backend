package edu.greenchannel.workstudy.controller;

import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.workstudy.entity.WorkStudyApply;
import edu.greenchannel.workstudy.service.WorkStudyApplyService;
import edu.greenchannel.workstudy.service.WorkStudyAttendanceService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkStudyIdentityBindingTest {

    @Test
    void applicationUsesAuthenticatedStudentInsteadOfRequestBodyIdentity() {
        WorkStudyApplyService service = mock(WorkStudyApplyService.class);
        when(service.applyForPosition(any(), any(), any())).thenReturn(3L);
        WorkStudyApplyController controller = new WorkStudyApplyController(service);
        WorkStudyApply body = new WorkStudyApply();
        body.setStudentId(999L);

        controller.submitApply(5L, user(77L), body);

        verify(service).applyForPosition(5L, 77L, body);
    }

    @Test
    void checkoutUsesAuthenticatedStudent() {
        WorkStudyAttendanceService service = mock(WorkStudyAttendanceService.class);
        WorkStudyAttendanceController controller = new WorkStudyAttendanceController(service);

        controller.checkOut(12L, user(77L));

        verify(service).checkOut(12L, 77L);
    }

    private CurrentUser user(long id) {
        return new CurrentUser(id, "student", "学生", 1, "学生",
                List.of(), List.of("student:workstudy:view"), List.of());
    }
}

package edu.greenchannel.workstudy.controller;

import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.workstudy.entity.WorkStudyApply;
import edu.greenchannel.workstudy.service.WorkStudyApplyService;
import edu.greenchannel.workstudy.service.WorkStudyAttendanceService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WorkStudyIdentityBindingTest {

    @Test
    void applicationUsesAuthenticatedStudentInsteadOfRequestBodyIdentity() {
        WorkStudyApplyService service = mock(WorkStudyApplyService.class);
        when(service.applyForPosition(anyLong(), anyLong(), any(WorkStudyApply.class)))
                .thenReturn(3L);

        WorkStudyApplyController controller = new WorkStudyApplyController(service);
        WorkStudyApply body = new WorkStudyApply();
        body.setPositionId(5L);  // 设置positionId，因为Controller从这里读取
        body.setStudentId(999L); // 这个值应该被忽略

        controller.submitApply(body, user(77L));

        verify(service).applyForPosition(eq(5L), eq(77L), eq(body));

        assertEquals(77L, body.getStudentId(), "Student ID should be overwritten");
    }

    @Test
    void checkoutUsesAuthenticatedStudent() {
        WorkStudyAttendanceService service = mock(WorkStudyAttendanceService.class);
        WorkStudyAttendanceController controller = new WorkStudyAttendanceController(service);
        controller.checkOut(12L, user(77L));
        verify(service).checkOut(eq(12L), eq(77L));
    }

    private CurrentUser user(long id) {
        return new CurrentUser(
                id,
                String.valueOf(id),  // username必须是数字字符串！
                "学生",
                1,
                "学生",
                List.of(),
                List.of("student:workstudy:view"),
                List.of()
        );
    }
}
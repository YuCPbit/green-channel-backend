package edu.greenchannel.workstudy.controller;

import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.workstudy.entity.WorkStudyApply;
import edu.greenchannel.workstudy.service.WorkStudyApplyService;
import edu.greenchannel.workstudy.service.WorkStudyAttendanceService;
import edu.greenchannel.workstudy.service.WorkStudyIdentityService;
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
        WorkStudyIdentityService identityService = mock(WorkStudyIdentityService.class);
        when(identityService.requireStudentId(700L)).thenReturn(77L);

        WorkStudyApplyController controller = new WorkStudyApplyController(service, identityService);
        WorkStudyApply body = new WorkStudyApply();
        body.setPositionId(5L);  // 设置positionId，因为Controller从这里读取
        body.setStudentId(999L); // 这个值应该被忽略

        controller.submitApply(body, user(700L));

        verify(service).applyForPosition(eq(5L), eq(77L), eq(body));

        assertEquals(77L, body.getStudentId(), "Student ID should be overwritten");
    }

    @Test
    void checkoutUsesAuthenticatedStudent() {
        WorkStudyAttendanceService service = mock(WorkStudyAttendanceService.class);
        WorkStudyIdentityService identityService = mock(WorkStudyIdentityService.class);
        when(identityService.requireStudentId(700L)).thenReturn(77L);
        WorkStudyAttendanceController controller =
                new WorkStudyAttendanceController(service, identityService);
        controller.checkOut(12L, user(700L));
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

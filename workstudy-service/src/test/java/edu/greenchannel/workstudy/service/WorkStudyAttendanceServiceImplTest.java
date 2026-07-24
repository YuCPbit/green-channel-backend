package edu.greenchannel.workstudy.service.impl;

import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.entity.WorkStudyAttendance;
import edu.greenchannel.workstudy.entity.WorkStudyHire;
import edu.greenchannel.workstudy.mapper.WorkStudyAttendanceMapper;
import edu.greenchannel.workstudy.service.WorkStudyHireService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WorkStudyAttendanceServiceImplTest {

    private final WorkStudyAttendanceMapper attendanceMapper = mock(WorkStudyAttendanceMapper.class);
    private final WorkStudyHireService hireService = mock(WorkStudyHireService.class);
    private final WorkStudyAttendanceServiceImpl service = spy(new WorkStudyAttendanceServiceImpl(
            attendanceMapper, hireService));

    {
        injectBaseMapper(service, attendanceMapper);
    }

    private static void injectBaseMapper(Object service, Object mapper) {
        try {
            Class<?> clazz = service.getClass().getSuperclass();
            while (clazz != null) {
                try {
                    var field = clazz.getDeclaredField("baseMapper");
                    field.setAccessible(true);
                    field.set(service, mapper);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final long HIRE_ID = 100L;
    private static final long STUDENT_ID = 200L;
    private static final long OTHER_STUDENT_ID = 999L;
    private static final long ATTENDANCE_ID = 1L;

    private static WorkStudyHire activeHire() {
        WorkStudyHire hire = new WorkStudyHire();
        hire.setId(HIRE_ID);
        hire.setStudentId(STUDENT_ID);
        hire.setHireStatus(1);
        hire.setIsDeleted(0);
        return hire;
    }

    // ========================================================================
    // validateHireOwnership — 校验录用关系（checkIn / applyLeave / applyRepair 共用）
    // ========================================================================

    @Nested
    class ValidateHireOwnership {

        @Test
        void shouldRejectWhenHireNotFound() {
            when(hireService.getById(HIRE_ID)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.checkIn(HIRE_ID, STUDENT_ID, "图书馆"));
            assertEquals(40400, ex.getCode());
            assertTrue(ex.getMessage().contains("录用记录不存在"));
        }

        @Test
        void shouldRejectWhenHireNotActive() {
            WorkStudyHire hire = activeHire();
            hire.setHireStatus(2);
            when(hireService.getById(HIRE_ID)).thenReturn(hire);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.checkIn(HIRE_ID, STUDENT_ID, "图书馆"));
            assertEquals(40900, ex.getCode());
            assertTrue(ex.getMessage().contains("不在岗"));
        }

        @Test
        void shouldRejectWhenStudentIdMismatchForCheckIn() {
            when(hireService.getById(HIRE_ID)).thenReturn(activeHire());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.checkIn(HIRE_ID, OTHER_STUDENT_ID, "图书馆"));
            assertEquals(40300, ex.getCode());
            assertTrue(ex.getMessage().contains("不属于当前学生"));
        }

        @Test
        void shouldRejectWhenStudentIdMismatchForLeave() {
            when(hireService.getById(HIRE_ID)).thenReturn(activeHire());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.applyLeave(HIRE_ID, OTHER_STUDENT_ID, LocalDate.now(), 1, "事假"));
            assertEquals(40300, ex.getCode());
        }

        @Test
        void shouldRejectWhenStudentIdMismatchForRepair() {
            when(hireService.getById(HIRE_ID)).thenReturn(activeHire());

            LocalDate date = LocalDate.of(2024, 6, 1);
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.applyRepair(HIRE_ID, OTHER_STUDENT_ID, date,
                            date.atTime(8, 30), date.atTime(17, 30), "补卡"));
            assertEquals(40300, ex.getCode());
        }
    }

    // ========================================================================
    // checkOut — 学生身份校验
    // ========================================================================

    @Nested
    class CheckOut {

        @Test
        void shouldRejectWhenCheckOutOthersAttendance() {
            WorkStudyAttendance attendance = new WorkStudyAttendance();
            attendance.setId(ATTENDANCE_ID);
            attendance.setHireId(HIRE_ID);
            attendance.setStudentId(STUDENT_ID);
            doReturn(attendance).when(service).getById(ATTENDANCE_ID);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.checkOut(ATTENDANCE_ID, OTHER_STUDENT_ID));
            assertEquals(40300, ex.getCode());
            assertTrue(ex.getMessage().contains("无权签退他人的考勤记录"));
        }
    }

    // ========================================================================
    // approveAttendance
    // ========================================================================

    @Nested
    class ApproveAttendance {

        @Test
        void shouldRejectWhenAlreadyApproved() {
            WorkStudyAttendance attendance = new WorkStudyAttendance();
            attendance.setId(ATTENDANCE_ID);
            attendance.setApprovalStatus(2);
            doReturn(attendance).when(service).getById(ATTENDANCE_ID);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.approveAttendance(ATTENDANCE_ID, 1L, true));
            assertEquals(40000, ex.getCode());
            assertTrue(ex.getMessage().contains("已审批"));
        }

        @Test
        void shouldRejectWhenAttendanceNotFound() {
            doReturn(null).when(service).getById(ATTENDANCE_ID);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.approveAttendance(ATTENDANCE_ID, 1L, true));
            assertEquals(40000, ex.getCode());
            assertTrue(ex.getMessage().contains("考勤记录不存在"));
        }
    }
}

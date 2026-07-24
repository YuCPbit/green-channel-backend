package edu.greenchannel.workstudy.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.dto.WorkStudyEvaluationVO;
import edu.greenchannel.workstudy.entity.WorkStudyEvaluation;
import edu.greenchannel.workstudy.entity.WorkStudyHire;
import edu.greenchannel.workstudy.mapper.WorkStudyEvaluationMapper;
import edu.greenchannel.workstudy.service.NotificationService;
import edu.greenchannel.workstudy.service.WorkStudyHireService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WorkStudyEvaluationServiceImplTest {

    private final WorkStudyEvaluationMapper evaluationMapper = mock(WorkStudyEvaluationMapper.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final WorkStudyHireService hireService = mock(WorkStudyHireService.class);
    private final WorkStudyEvaluationServiceImpl service = spy(new WorkStudyEvaluationServiceImpl(
            evaluationMapper, notificationService, hireService));

    {
        injectBaseMapper(service, evaluationMapper);
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
    private static final long USER_ID = 300L;
    private static final long EVAL_ID = 1L;
    private static final long OTHER_STUDENT_ID = 999L;

    private static WorkStudyHire activeHire() {
        WorkStudyHire hire = new WorkStudyHire();
        hire.setId(HIRE_ID);
        hire.setStudentId(STUDENT_ID);
        hire.setHireStatus(1);
        hire.setIsDeleted(0);
        return hire;
    }

    private static WorkStudyEvaluation validEvaluation() {
        WorkStudyEvaluation eval = new WorkStudyEvaluation();
        eval.setHireId(HIRE_ID);
        eval.setStudentId(STUDENT_ID);
        eval.setEvalYear(2024);
        eval.setEvalMonth(6);
        eval.setScore(4);
        eval.setComment("表现良好");
        eval.setEvaluatorId(10L);
        return eval;
    }

    // ========================================================================
    // submitEvaluation — 校验录用关系
    // ========================================================================

    @Nested
    class SubmitEvaluation {

        @Test
        void shouldRejectWhenScoreOutOfRange() {
            WorkStudyEvaluation eval = validEvaluation();
            eval.setScore(0);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.submitEvaluation(eval));
            assertEquals(40000, ex.getCode());
            assertTrue(ex.getMessage().contains("评分必须在1-5之间"));
            verify(service, never()).save(any(WorkStudyEvaluation.class));
        }

        @Test
        void shouldRejectWhenHireNotFound() {
            when(hireService.getById(HIRE_ID)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.submitEvaluation(validEvaluation()));
            assertEquals(40400, ex.getCode());
            assertTrue(ex.getMessage().contains("录用记录不存在"));
            verify(service, never()).save(any(WorkStudyEvaluation.class));
        }

        @Test
        void shouldRejectWhenHireNotActive() {
            WorkStudyHire hire = activeHire();
            hire.setHireStatus(3);
            when(hireService.getById(HIRE_ID)).thenReturn(hire);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.submitEvaluation(validEvaluation()));
            assertEquals(40900, ex.getCode());
            assertTrue(ex.getMessage().contains("不在岗"));
            verify(service, never()).save(any(WorkStudyEvaluation.class));
        }

        @Test
        void shouldRejectWhenStudentIdMismatch() {
            when(hireService.getById(HIRE_ID)).thenReturn(activeHire());
            WorkStudyEvaluation eval = validEvaluation();
            eval.setStudentId(OTHER_STUDENT_ID);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.submitEvaluation(eval));
            assertEquals(40300, ex.getCode());
            assertTrue(ex.getMessage().contains("不匹配"));
            verify(service, never()).save(any(WorkStudyEvaluation.class));
        }
    }

    // ========================================================================
    // getMyEvaluations — 防越权
    // ========================================================================

    @Nested
    class GetMyEvaluations {

        @Test
        void shouldReturnOwnEvaluationsOnly() {
            CurrentUser studentUser = new CurrentUser(USER_ID, "student1", "学生A", 1,
                    "学生", List.of(), List.of("school:workstudy:view"), List.of());
            when(evaluationMapper.findStudentIdByUserId(USER_ID)).thenReturn(STUDENT_ID);
            when(evaluationMapper.selectEvaluationPage(any(Page.class), any()))
                    .thenReturn(new Page<>(1, 20));

            var result = service.getMyEvaluations(1, 20, null, null, studentUser);
            assertNotNull(result);
            verify(evaluationMapper).findStudentIdByUserId(USER_ID);
        }

        @Test
        void shouldRejectWhenStudentNotFound() {
            CurrentUser studentUser = new CurrentUser(USER_ID, "student1", "学生A", 1,
                    "学生", List.of(), List.of("school:workstudy:view"), List.of());
            when(evaluationMapper.findStudentIdByUserId(USER_ID)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.getMyEvaluations(1, 20, null, null, studentUser));
            assertEquals(40400, ex.getCode());
            assertTrue(ex.getMessage().contains("未找到您的学生档案"));
        }
    }

    // ========================================================================
    // updateEvaluation
    // ========================================================================

    @Nested
    class UpdateEvaluation {

        @Test
        void shouldUpdateSuccessfully() {
            WorkStudyEvaluation existing = validEvaluation();
            existing.setId(EVAL_ID);
            doReturn(existing).when(service).getById(EVAL_ID);
            doReturn(true).when(service).updateById(any(WorkStudyEvaluation.class));

            assertDoesNotThrow(() -> service.updateEvaluation(EVAL_ID, 5, "更新评语", USER_ID));
            verify(service).updateById(any(WorkStudyEvaluation.class));
        }

        @Test
        void shouldRejectWhenNotFound() {
            doReturn(null).when(service).getById(EVAL_ID);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.updateEvaluation(EVAL_ID, 5, "评语", USER_ID));
            assertEquals(40400, ex.getCode());
        }

        @Test
        void shouldRejectWhenScoreOutOfRange() {
            WorkStudyEvaluation existing = validEvaluation();
            existing.setId(EVAL_ID);
            doReturn(existing).when(service).getById(EVAL_ID);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.updateEvaluation(EVAL_ID, 6, "评语", USER_ID));
            assertEquals(40000, ex.getCode());
            assertTrue(ex.getMessage().contains("评分必须在1-5之间"));
        }
    }

    // ========================================================================
    // deleteEvaluation
    // ========================================================================

    @Nested
    class DeleteEvaluation {

        @Test
        void shouldDeleteSuccessfully() {
            WorkStudyEvaluation existing = validEvaluation();
            existing.setId(EVAL_ID);
            doReturn(existing).when(service).getById(EVAL_ID);
            doReturn(true).when(service).removeById(EVAL_ID);

            assertDoesNotThrow(() -> service.deleteEvaluation(EVAL_ID));
            verify(service).removeById(EVAL_ID);
        }

        @Test
        void shouldRejectWhenNotFound() {
            doReturn(null).when(service).getById(EVAL_ID);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.deleteEvaluation(EVAL_ID));
            assertEquals(40400, ex.getCode());
        }
    }

    // ========================================================================
    // getDetail
    // ========================================================================

    @Nested
    class GetDetail {

        @Test
        void shouldReturnDetail() {
            WorkStudyEvaluationVO vo = new WorkStudyEvaluationVO();
            vo.setId(EVAL_ID);
            vo.setStudentName("张三");
            when(evaluationMapper.selectEvaluationById(EVAL_ID)).thenReturn(vo);

            WorkStudyEvaluationVO result = service.getDetail(EVAL_ID);
            assertNotNull(result);
            assertEquals("张三", result.getStudentName());
        }

        @Test
        void shouldRejectWhenNotFound() {
            when(evaluationMapper.selectEvaluationById(EVAL_ID)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.getDetail(EVAL_ID));
            assertEquals(40400, ex.getCode());
        }
    }
}

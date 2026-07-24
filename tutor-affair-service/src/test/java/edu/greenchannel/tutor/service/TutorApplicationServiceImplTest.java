package edu.greenchannel.tutor.service;

import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.common.PageResult;
import edu.greenchannel.tutor.dto.request.TutorApplyRequest;
import edu.greenchannel.tutor.dto.request.TutorReviewRequest;
import edu.greenchannel.tutor.dto.response.ApplyTypeResponse;
import edu.greenchannel.tutor.dto.response.TutorApplyView;
import edu.greenchannel.tutor.dto.response.TutorReviewView;
import edu.greenchannel.tutor.entity.TutorApplication;
import edu.greenchannel.tutor.entity.TutorApplyType;
import edu.greenchannel.tutor.entity.TutorAppReview;
import edu.greenchannel.tutor.enums.TutorAppStatus;
import edu.greenchannel.tutor.repository.TutorApplicationRepository;
import edu.greenchannel.tutor.repository.TutorApplyTypeRepository;
import edu.greenchannel.tutor.repository.TutorAppReviewRepository;
import edu.greenchannel.tutor.repository.TutorAppStudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TutorApplicationServiceImplTest {

    private final TutorApplyTypeRepository typeRepository = mock(TutorApplyTypeRepository.class);
    private final TutorApplicationRepository applicationRepository = mock(TutorApplicationRepository.class);
    private final TutorAppStudentRepository studentRelRepository = mock(TutorAppStudentRepository.class);
    private final TutorAppReviewRepository reviewRepository = mock(TutorAppReviewRepository.class);
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final TutorApplicationServiceImpl service = new TutorApplicationServiceImpl(
            typeRepository, applicationRepository, studentRelRepository, reviewRepository, jdbcTemplate);

    private static final long TUTOR_ID = 10L;
    private static final long COLLEGE_ADMIN_ID = 20L;
    private static final long SCHOOL_ADMIN_ID = 30L;
    private static final long STUDENT_ID = 40L;
    private static final long APP_ID = 1L;
    private static final long TYPE_ID = 100L;
    private static final long COLLEGE_ID = 5L;
    private static final long OTHER_COLLEGE_ID = 6L;

    // ---- CurrentUser helpers ----
    private static CurrentUser tutor() {
        return new CurrentUser(TUTOR_ID, "tutor1", "辅导员A", 2, "辅导员",
                List.of(), List.of("tutor:application:view"), List.of());
    }

    private static CurrentUser collegeAdmin() {
        return new CurrentUser(COLLEGE_ADMIN_ID, "college", "学院管理员", 3, "学院管理员",
                List.of(), List.of("college:tutor-review:view"), List.of());
    }

    private static CurrentUser schoolAdmin() {
        return new CurrentUser(SCHOOL_ADMIN_ID, "admin", "学校管理员", 4, "学校资助中心",
                List.of(), List.of("school:tutor-disburse:view"), List.of());
    }

    private static CurrentUser student() {
        return new CurrentUser(STUDENT_ID, "student", "学生", 1, "学生",
                List.of(), List.of(), List.of());
    }

    // ---- Entity builders ----
    private static TutorApplyType buildType(int needAmount, int needStudent, int approvalLevel) {
        TutorApplyType type = new TutorApplyType();
        type.setId(TYPE_ID);
        type.setTypeName("测试申请类型");
        type.setTypeCode("TEST");
        type.setNeedAmount(needAmount);
        type.setNeedStudent(needStudent);
        type.setApprovalLevel(approvalLevel);
        type.setStatus(1);
        return type;
    }

    private static TutorApplication buildApp(long id, long tutorId, int status) {
        TutorApplication app = new TutorApplication();
        app.setId(id);
        app.setApplyNo("TA20240001");
        app.setTypeId(TYPE_ID);
        app.setTutorId(tutorId);
        app.setTitle("测试申请");
        app.setDescription("测试描述");
        app.setAmount(new BigDecimal("500"));
        app.setUrgency(1);
        app.setStatus(status);
        app.setDisburseStatus(0);
        return app;
    }

    // ========================================================================
    // createApplication
    // ========================================================================

    @Nested
    class CreateApplication {

        private TutorApplyRequest validRequest() {
            return new TutorApplyRequest(TYPE_ID, "测试申请", "测试描述",
                    new BigDecimal("500"), 1, null, null);
        }

        @Test
        void shouldCreateDraftSuccessfully() {
            TutorApplyType type = buildType(1, 0, 2);
            when(typeRepository.findById(TYPE_ID)).thenReturn(Optional.of(type));
            when(applicationRepository.save(any(TutorApplication.class))).thenAnswer(inv -> {
                TutorApplication a = inv.getArgument(0);
                a.setId(APP_ID);
                return a;
            });
            // mock buildApplyView dependencies
            when(typeRepository.findById(TYPE_ID)).thenReturn(Optional.of(type));
            when(reviewRepository.findByApplicationIdAndIsDeletedOrderByReviewTimeAsc(eq(APP_ID), eq(0)))
                    .thenReturn(Collections.emptyList());
            when(studentRelRepository.findByApplicationIdAndIsDeleted(eq(APP_ID), eq(0)))
                    .thenReturn(Collections.emptyList());

            TutorApplyView result = service.createApplication(tutor(), validRequest());
            assertNotNull(result);
            assertEquals(APP_ID, result.id());
            verify(applicationRepository).save(any(TutorApplication.class));
        }

        @Test
        void shouldRejectWhenTypeNotFound() {
            when(typeRepository.findById(TYPE_ID)).thenReturn(Optional.empty());
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createApplication(tutor(), validRequest()));
            assertEquals(40000, ex.getCode());
            assertTrue(ex.getMessage().contains("申请类型不存在"));
        }

        @Test
        void shouldRejectWhenNegativeAmount() {
            TutorApplyType type = buildType(1, 0, 2);
            when(typeRepository.findById(TYPE_ID)).thenReturn(Optional.of(type));
            TutorApplyRequest request = new TutorApplyRequest(TYPE_ID, "测试", "描述",
                    new BigDecimal("-100"), 1, null, null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createApplication(tutor(), request));
            assertEquals(40000, ex.getCode());
            assertTrue(ex.getMessage().contains("不能为负数"));
        }

        @Test
        void shouldRejectWhenNeedStudentButNoStudentIds() {
            TutorApplyType type = buildType(0, 1, 2);
            when(typeRepository.findById(TYPE_ID)).thenReturn(Optional.of(type));
            List<Long> emptyStudentIds = List.of();
            TutorApplyRequest request = new TutorApplyRequest(TYPE_ID, "测试", "描述",
                    null, 1, emptyStudentIds, null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createApplication(tutor(), request));
            assertEquals(40000, ex.getCode());
            assertTrue(ex.getMessage().contains("需要关联至少一个学生"));
        }
    }

    // ========================================================================
    // submitDraft
    // ========================================================================

    @Nested
    class SubmitDraft {

        private void mockBuildViewDeps(long appId) {
            when(typeRepository.findById(TYPE_ID)).thenReturn(Optional.of(buildType(1, 0, 2)));
            when(reviewRepository.findByApplicationIdAndIsDeletedOrderByReviewTimeAsc(eq(appId), eq(0)))
                    .thenReturn(Collections.emptyList());
            when(studentRelRepository.findByApplicationIdAndIsDeleted(eq(appId), eq(0)))
                    .thenReturn(Collections.emptyList());
        }

        @Test
        void shouldSubmitDraftSuccessfully() {
            TutorApplication app = buildApp(APP_ID, TUTOR_ID, TutorAppStatus.DRAFT.getCode());
            when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));
            when(applicationRepository.save(any(TutorApplication.class))).thenAnswer(inv -> inv.getArgument(0));
            mockBuildViewDeps(APP_ID);

            TutorApplyView result = service.submitDraft(tutor(), APP_ID);
            assertEquals(TutorAppStatus.PENDING_COLLEGE.getCode(), result.status());
        }

        @Test
        void shouldRejectWhenNotOwner() {
            TutorApplication app = buildApp(APP_ID, 99L, TutorAppStatus.DRAFT.getCode());
            when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.submitDraft(tutor(), APP_ID));
            assertEquals(40300, ex.getCode());
            assertTrue(ex.getMessage().contains("无权操作"));
        }

        @Test
        void shouldRejectWhenNotDraftOrRejected() {
            TutorApplication app = buildApp(APP_ID, TUTOR_ID, TutorAppStatus.APPROVED.getCode());
            when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.submitDraft(tutor(), APP_ID));
            assertEquals(40900, ex.getCode());
        }
    }

    // ========================================================================
    // getDetail — visibility
    // ========================================================================

    @Nested
    class GetDetail {

        private void mockBuildViewDeps(long appId) {
            when(typeRepository.findById(TYPE_ID)).thenReturn(Optional.of(buildType(1, 0, 2)));
            when(reviewRepository.findByApplicationIdAndIsDeletedOrderByReviewTimeAsc(eq(appId), eq(0)))
                    .thenReturn(Collections.emptyList());
            when(studentRelRepository.findByApplicationIdAndIsDeleted(eq(appId), eq(0)))
                    .thenReturn(Collections.emptyList());
        }

        @Test
        void tutorShouldSeeOwnApplication() {
            TutorApplication app = buildApp(APP_ID, TUTOR_ID, TutorAppStatus.DRAFT.getCode());
            when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));
            mockBuildViewDeps(APP_ID);

            TutorApplyView result = service.getDetail(tutor(), APP_ID);
            assertNotNull(result);
            assertEquals(TUTOR_ID, result.tutorId());
        }

        @Test
        void tutorShouldNotSeeOtherTutorApplication() {
            TutorApplication app = buildApp(APP_ID, 99L, TutorAppStatus.DRAFT.getCode());
            when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.getDetail(tutor(), APP_ID));
            assertEquals(40300, ex.getCode());
        }

        @Test
        void collegeAdminShouldSeeOwnCollegeApplication() {
            TutorApplication app = buildApp(APP_ID, TUTOR_ID, TutorAppStatus.APPROVED.getCode());
            when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));
            when(jdbcTemplate.queryForObject(
                    anyString(), eq(Long.class), eq(COLLEGE_ADMIN_ID)))
                    .thenReturn(COLLEGE_ID);
            when(jdbcTemplate.queryForObject(
                    anyString(), eq(Long.class), eq(TUTOR_ID)))
                    .thenReturn(COLLEGE_ID);
            mockBuildViewDeps(APP_ID);

            TutorApplyView result = service.getDetail(collegeAdmin(), APP_ID);
            assertNotNull(result);
        }

        @Test
        void collegeAdminShouldNotSeeOtherCollegeApplication() {
            TutorApplication app = buildApp(APP_ID, TUTOR_ID, TutorAppStatus.APPROVED.getCode());
            when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));
            when(jdbcTemplate.queryForObject(
                    anyString(), eq(Long.class), eq(COLLEGE_ADMIN_ID)))
                    .thenReturn(COLLEGE_ID);
            when(jdbcTemplate.queryForObject(
                    anyString(), eq(Long.class), eq(TUTOR_ID)))
                    .thenReturn(OTHER_COLLEGE_ID);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.getDetail(collegeAdmin(), APP_ID));
            assertEquals(40300, ex.getCode());
        }

        @Test
        void schoolAdminShouldSeeAllApplications() {
            TutorApplication app = buildApp(APP_ID, TUTOR_ID, TutorAppStatus.APPROVED.getCode());
            when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));
            mockBuildViewDeps(APP_ID);

            TutorApplyView result = service.getDetail(schoolAdmin(), APP_ID);
            assertNotNull(result);
        }

        @Test
        void studentShouldBeRejected() {
            TutorApplication app = buildApp(APP_ID, TUTOR_ID, TutorAppStatus.DRAFT.getCode());
            when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.getDetail(student(), APP_ID));
            assertEquals(40300, ex.getCode());
        }
    }

    // ========================================================================
    // submitReview — permission
    // ========================================================================

    @Nested
    class SubmitReview {

        private TutorReviewRequest passRequest() {
            return new TutorReviewRequest(APP_ID, 1, "审核通过"); // action=1 通过
        }

        private void mockReviewDeps(TutorApplication app, TutorApplyType type) {
            when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));
            when(typeRepository.findById(TYPE_ID)).thenReturn(Optional.of(type));
        }

        @Test
        void collegeAdminShouldReviewPendingCollege() {
            TutorApplication app = buildApp(APP_ID, TUTOR_ID, TutorAppStatus.PENDING_COLLEGE.getCode());
            TutorApplyType type = buildType(1, 0, 1); // approvalLevel=1 只需学院审批
            mockReviewDeps(app, type);

            when(reviewRepository.save(any(TutorAppReview.class))).thenAnswer(inv -> inv.getArgument(0));
            when(applicationRepository.save(any(TutorApplication.class))).thenAnswer(inv -> inv.getArgument(0));

            assertDoesNotThrow(() -> service.submitReview(collegeAdmin(), passRequest()));
            verify(reviewRepository).save(any(TutorAppReview.class));
        }

        @Test
        void schoolAdminShouldReviewPendingSchool() {
            TutorApplication app = buildApp(APP_ID, TUTOR_ID, TutorAppStatus.PENDING_SCHOOL.getCode());
            TutorApplyType type = buildType(1, 0, 2);
            mockReviewDeps(app, type);

            when(reviewRepository.save(any(TutorAppReview.class))).thenAnswer(inv -> inv.getArgument(0));
            when(applicationRepository.save(any(TutorApplication.class))).thenAnswer(inv -> inv.getArgument(0));

            assertDoesNotThrow(() -> service.submitReview(schoolAdmin(), passRequest()));
            verify(reviewRepository).save(any(TutorAppReview.class));
        }

        @Test
        void tutorShouldNotSubmitCollegeReview() {
            TutorApplication app = buildApp(APP_ID, TUTOR_ID, TutorAppStatus.PENDING_COLLEGE.getCode());
            TutorApplyType type = buildType(1, 0, 2);
            mockReviewDeps(app, type);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.submitReview(tutor(), passRequest()));
            assertEquals(40300, ex.getCode());
            assertTrue(ex.getMessage().contains("仅学院管理员"));
        }

        @Test
        void collegeAdminShouldNotSubmitSchoolReview() {
            TutorApplication app = buildApp(APP_ID, TUTOR_ID, TutorAppStatus.PENDING_SCHOOL.getCode());
            TutorApplyType type = buildType(1, 0, 2);
            mockReviewDeps(app, type);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.submitReview(collegeAdmin(), passRequest()));
            assertEquals(40300, ex.getCode());
            assertTrue(ex.getMessage().contains("仅学校管理员"));
        }

        @Test
        void shouldRejectWhenWrongStatus() {
            TutorApplication app = buildApp(APP_ID, TUTOR_ID, TutorAppStatus.DRAFT.getCode());
            TutorApplyType type = buildType(1, 0, 2);
            mockReviewDeps(app, type);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.submitReview(collegeAdmin(), passRequest()));
            assertEquals(40900, ex.getCode());
        }

        @Test
        void schoolAdminCanDoFinalRecord() {
            // action=4 备案
            TutorReviewRequest recordRequest = new TutorReviewRequest(APP_ID, 4, "备案");
            TutorApplication app = buildApp(APP_ID, TUTOR_ID, TutorAppStatus.PENDING_SCHOOL.getCode());
            TutorApplyType type = buildType(1, 0, 2);
            mockReviewDeps(app, type);

            when(reviewRepository.save(any(TutorAppReview.class))).thenAnswer(inv -> inv.getArgument(0));
            when(applicationRepository.save(any(TutorApplication.class))).thenAnswer(inv -> inv.getArgument(0));

            assertDoesNotThrow(() -> service.submitReview(schoolAdmin(), recordRequest));
            verify(reviewRepository).save(any(TutorAppReview.class));
        }
    }

    // ========================================================================
    // disburse — permission
    // ========================================================================

    @Nested
    class Disburse {

        @Test
        void schoolAdminShouldDisburseSuccessfully() {
            TutorApplication app = buildApp(APP_ID, TUTOR_ID, TutorAppStatus.APPROVED.getCode());
            app.setDisburseStatus(1); // 待下发
            when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));
            when(applicationRepository.save(any(TutorApplication.class))).thenAnswer(inv -> inv.getArgument(0));

            assertDoesNotThrow(() -> service.disburse(schoolAdmin(), APP_ID));
            verify(applicationRepository).save(any(TutorApplication.class));
        }

        @Test
        void collegeAdminShouldBeRejected() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.disburse(collegeAdmin(), APP_ID));
            assertEquals(40300, ex.getCode());
            assertTrue(ex.getMessage().contains("仅学校资助中心"));
            verify(applicationRepository, never()).findById(anyLong());
        }

        @Test
        void tutorShouldBeRejected() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.disburse(tutor(), APP_ID));
            assertEquals(40300, ex.getCode());
            assertTrue(ex.getMessage().contains("仅学校资助中心"));
        }

        @Test
        void studentShouldBeRejected() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.disburse(student(), APP_ID));
            assertEquals(40300, ex.getCode());
        }

        @Test
        void shouldRejectWhenNotApproved() {
            TutorApplication app = buildApp(APP_ID, TUTOR_ID, TutorAppStatus.PENDING_SCHOOL.getCode());
            when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.disburse(schoolAdmin(), APP_ID));
            assertEquals(40900, ex.getCode());
            assertTrue(ex.getMessage().contains("仅已通过的申请"));
        }

        @Test
        void shouldRejectWhenAlreadyDisbursed() {
            TutorApplication app = buildApp(APP_ID, TUTOR_ID, TutorAppStatus.APPROVED.getCode());
            app.setDisburseStatus(2); // 已下发
            when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.disburse(schoolAdmin(), APP_ID));
            assertEquals(40900, ex.getCode());
            assertTrue(ex.getMessage().contains("已下发"));
        }

        @Test
        void shouldRejectWhenNoAmount() {
            TutorApplication app = buildApp(APP_ID, TUTOR_ID, TutorAppStatus.APPROVED.getCode());
            app.setAmount(BigDecimal.ZERO);
            app.setDisburseStatus(1);
            when(applicationRepository.findById(APP_ID)).thenReturn(Optional.of(app));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.disburse(schoolAdmin(), APP_ID));
            assertEquals(40900, ex.getCode());
            assertTrue(ex.getMessage().contains("不涉及资金下发"));
        }
    }

    // ========================================================================
    // batchDisburse — permission
    // ========================================================================

    @Nested
    class BatchDisburse {

        @Test
        void schoolAdminShouldBatchDisburseSuccessfully() {
            TutorApplication app1 = buildApp(1L, TUTOR_ID, TutorAppStatus.APPROVED.getCode());
            app1.setDisburseStatus(1);
            TutorApplication app2 = buildApp(2L, TUTOR_ID, TutorAppStatus.APPROVED.getCode());
            app2.setDisburseStatus(1);
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(app1));
            when(applicationRepository.findById(2L)).thenReturn(Optional.of(app2));
            when(applicationRepository.save(any(TutorApplication.class))).thenAnswer(inv -> inv.getArgument(0));

            int count = service.batchDisburse(schoolAdmin(), List.of(1L, 2L));
            assertEquals(2, count);
        }

        @Test
        void collegeAdminShouldBeRejected() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.batchDisburse(collegeAdmin(), List.of(1L)));
            assertEquals(40300, ex.getCode());
            assertTrue(ex.getMessage().contains("仅学校资助中心"));
        }

        @Test
        void tutorShouldBeRejected() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.batchDisburse(tutor(), List.of(1L)));
            assertEquals(40300, ex.getCode());
        }

        @Test
        void shouldRejectWhenEmptyList() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.batchDisburse(schoolAdmin(), Collections.emptyList()));
            assertEquals(40000, ex.getCode());
        }

        @Test
        void shouldReturnZeroWhenNoMatch() {
            // 所有申请都不符合条件 → 跳过所有
            TutorApplication app = buildApp(1L, TUTOR_ID, TutorAppStatus.DRAFT.getCode());
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.batchDisburse(schoolAdmin(), List.of(1L)));
            assertEquals(40000, ex.getCode());
            assertTrue(ex.getMessage().contains("均不符合下发条件"));
        }
    }

    // ========================================================================
    // listDisburse — permission
    // ========================================================================

    @Nested
    class ListDisburse {

        @Test
        void schoolAdminShouldSeeList() {
            when(applicationRepository.findForReviewNative(
                    eq(TutorAppStatus.APPROVED.getCode()), isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            PageResult<TutorApplyView> result = service.listDisburse(schoolAdmin(), null, null, 1, 20);
            assertNotNull(result);
        }

        @Test
        void collegeAdminShouldBeRejected() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.listDisburse(collegeAdmin(), null, null, 1, 20));
            assertEquals(40300, ex.getCode());
        }

        @Test
        void tutorShouldBeRejected() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.listDisburse(tutor(), null, null, 1, 20));
            assertEquals(40300, ex.getCode());
        }
    }

    // ========================================================================
    // getDisburseSummary — permission
    // ========================================================================

    @Nested
    class GetDisburseSummary {

        @Test
        void schoolAdminShouldGetSummary() {
            when(jdbcTemplate.queryForObject(anyString(), eq(Long.class),
                    eq(TutorAppStatus.APPROVED.getCode()))).thenReturn(5L, 10L);
            when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class),
                    eq(TutorAppStatus.APPROVED.getCode()))).thenReturn(new BigDecimal("5000"), new BigDecimal("10000"));

            var result = service.getDisburseSummary(schoolAdmin());
            assertEquals(5L, result.get("pendingCount"));
            assertEquals(10L, result.get("disbursedCount"));
        }

        @Test
        void collegeAdminShouldBeRejected() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.getDisburseSummary(collegeAdmin()));
            assertEquals(40300, ex.getCode());
        }

        @Test
        void tutorShouldBeRejected() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.getDisburseSummary(tutor()));
            assertEquals(40300, ex.getCode());
        }
    }

    // ========================================================================
    // getStatistics — permission
    // ========================================================================

    @Nested
    class GetStatistics {

        @Test
        void collegeAdminShouldGetStatistics() {
            List<Object[]> stats1 = List.<Object[]>of(
                    new Object[]{TutorAppStatus.DRAFT.getCode(), 3L},
                    new Object[]{TutorAppStatus.APPROVED.getCode(), 10L});
            when(applicationRepository.countByStatus()).thenReturn(stats1);
            when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), anyInt())).thenReturn(5L);

            var result = service.getStatistics(collegeAdmin());
            assertEquals(13L, result.get("total"));
        }

        @Test
        void schoolAdminShouldGetStatistics() {
            List<Object[]> stats2 = List.<Object[]>of(
                    new Object[]{TutorAppStatus.DRAFT.getCode(), 5L});
            when(applicationRepository.countByStatus()).thenReturn(stats2);
            when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), anyInt())).thenReturn(2L);

            var result = service.getStatistics(schoolAdmin());
            assertEquals(5L, result.get("total"));
        }

        @Test
        void tutorShouldBeRejected() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.getStatistics(tutor()));
            assertEquals(40300, ex.getCode());
        }

        @Test
        void studentShouldBeRejected() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.getStatistics(student()));
            assertEquals(40300, ex.getCode());
        }
    }

    // ========================================================================
    // listMyApplications — tutor access control
    // ========================================================================

    @Nested
    class ListMyApplications {

        private void mockBuildViewDeps(long appId) {
            when(typeRepository.findById(TYPE_ID)).thenReturn(Optional.of(buildType(1, 0, 2)));
            when(reviewRepository.findByApplicationIdAndIsDeletedOrderByReviewTimeAsc(eq(appId), eq(0)))
                    .thenReturn(Collections.emptyList());
            when(studentRelRepository.findByApplicationIdAndIsDeleted(eq(appId), eq(0)))
                    .thenReturn(Collections.emptyList());
        }

        @Test
        void tutorShouldSeeOnlyOwnApplications() {
            TutorApplication app = buildApp(APP_ID, TUTOR_ID, TutorAppStatus.DRAFT.getCode());
            when(applicationRepository.findByTutorId(eq(TUTOR_ID), isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(app)));
            mockBuildViewDeps(APP_ID);

            PageResult<TutorApplyView> result = service.listMyApplications(tutor(), null, null, 1, 20);
            assertEquals(1, result.total());
        }
    }

    // ========================================================================
    // getApplyTypes
    // ========================================================================

    @Nested
    class GetApplyTypes {

        @Test
        void shouldReturnActiveTypes() {
            when(typeRepository.findAllActive()).thenReturn(List.of(buildType(1, 0, 1)));

            List<ApplyTypeResponse> result = service.getApplyTypes();
            assertEquals(1, result.size());
        }
    }
}

package edu.greenchannel.tutor.service;

import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.common.PageResult;
import edu.greenchannel.tutor.dto.request.TutorApplyRequest;
import edu.greenchannel.tutor.dto.request.TutorReviewRequest;
import edu.greenchannel.tutor.dto.response.ApplyTypeResponse;
import edu.greenchannel.tutor.dto.response.StudentBrief;
import edu.greenchannel.tutor.dto.response.TutorApplyView;

import java.util.List;
import java.util.Map;

/**
 * 辅导员事务申请服务接口
 */
public interface TutorApplicationService {

    /** 获取所有启用的申请类型 */
    List<ApplyTypeResponse> getApplyTypes();

    /** 辅导员发起申请（保存草稿或直接提交） */
    TutorApplyView createApplication(CurrentUser user, TutorApplyRequest request);

    /** 更新草稿并重新提交 */
    TutorApplyView updateApplication(CurrentUser user, Long id, TutorApplyRequest request);

    /** 提交草稿 */
    TutorApplyView submitDraft(CurrentUser user, Long id);

    /** 辅导员查询自己的申请列表 */
    PageResult<TutorApplyView> listMyApplications(CurrentUser user, Integer status, Long typeId, int page, int size);

    /** 管理员（学院/学校）查询待审批列表 */
    PageResult<TutorApplyView> listPendingReviews(CurrentUser user, Integer status, Long typeId, Integer urgency, int page, int size);

    /** 获取申请详情（含审核记录和关联学生） */
    TutorApplyView getDetail(CurrentUser user, Long id);

    /** 提交审核 */
    void submitReview(CurrentUser user, TutorReviewRequest request);

    /** 搜索所管学生 */
    List<StudentBrief> searchStudents(CurrentUser user, String keyword);

    /** 获取统计信息 */
    Map<String, Object> getStatistics(CurrentUser user);

    /** 单笔资金下发 */
    void disburse(CurrentUser user, Long id);

    /** 批量资金下发 */
    int batchDisburse(CurrentUser user, List<Long> ids);

    /** 查询资金下发列表（支持按状态筛选） */
    PageResult<TutorApplyView> listDisburse(CurrentUser user, Integer disburseStatus, Long typeId, int page, int size);

    /** 资金下发汇总统计 */
    Map<String, Object> getDisburseSummary(CurrentUser user);
}

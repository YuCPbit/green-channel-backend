package edu.greenchannel.workstudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.entity.WorkStudyApply;
import edu.greenchannel.workstudy.entity.WorkStudyBatch;
import edu.greenchannel.workstudy.entity.WorkStudyHire;
import edu.greenchannel.workstudy.entity.WorkStudyPosition;
import edu.greenchannel.workstudy.enums.WorkStudyStatus;
import edu.greenchannel.workstudy.mapper.WorkStudyApplyMapper;
import edu.greenchannel.workstudy.mapper.WorkStudyBatchMapper;
import edu.greenchannel.workstudy.mapper.WorkStudyHireMapper;
import edu.greenchannel.workstudy.mapper.WorkStudyPositionMapper;
import edu.greenchannel.workstudy.service.WorkStudyApplyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WorkStudyApplyServiceImpl
        extends ServiceImpl<WorkStudyApplyMapper, WorkStudyApply>
        implements WorkStudyApplyService {

    private final WorkStudyPositionMapper positionMapper;
    private final WorkStudyBatchMapper batchMapper;
    private final WorkStudyHireMapper hireMapper;

    public WorkStudyApplyServiceImpl(WorkStudyPositionMapper positionMapper,
                                     WorkStudyBatchMapper batchMapper,
                                     WorkStudyHireMapper hireMapper) {
        this.positionMapper = positionMapper;
        this.batchMapper = batchMapper;
        this.hireMapper = hireMapper;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long applyForPosition(Long positionId, Long studentId, WorkStudyApply applyInfo) {
        // 1. 校验岗位是否存在且正在招聘
        WorkStudyPosition position = positionMapper.selectById(positionId);
        if (position == null || position.getDeleted() == 1) {
            throw new BusinessException(40400, "岗位不存在");
        }

        // 2. 校验岗位状态（只有已上架的岗位才能报名）
        if (position.getStatus() != WorkStudyStatus.POSITION_ONLINE.getCode()) {
            throw new BusinessException(40000, "岗位未开放报名");
        }

        // 3. 校验批次是否有效
        WorkStudyBatch batch = batchMapper.selectById(position.getBatchId());
        if (batch == null || batch.getDeleted() == 1) {
            throw new BusinessException(40000, "所属批次不存在");
        }

        // 4. 校验批次报名时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(batch.getRegisterStartTime()) || now.isAfter(batch.getRegisterEndTime())) {
            throw new BusinessException(40000, "不在报名时间内");
        }

        boolean isBlacklisted = hireMapper.exists(
                new LambdaQueryWrapper<WorkStudyHire>()
                        .inSql(
                                WorkStudyHire::getPositionId,
                                "SELECT id FROM gc_work_study_position WHERE batch_id = " + position.getBatchId() + " AND is_deleted = 0"
                        )
                        .eq(WorkStudyHire::getStudentId, studentId)
                        .eq(WorkStudyHire::getHireStatus, 4)
                        .eq(WorkStudyHire::getDeleted, 0)
        );

        if (isBlacklisted) {
            throw new BusinessException(40300, "您因违规被解聘，本学期内无法申请新岗位");
        }
        
        // 5. 校验是否已报名（唯一索引 uk_position_student）
        long count = count(new LambdaQueryWrapper<WorkStudyApply>()
                .eq(WorkStudyApply::getPositionId, positionId)
                .eq(WorkStudyApply::getStudentId, studentId)
                .eq(WorkStudyApply::getDeleted, 0));
        if (count > 0) {
            throw new BusinessException(40900, "您已报名该岗位，请勿重复提交");
        }

        // 6. 校验该学生在同批次的报名数量（最多3个）
        int batchApplyCount = countStudentApplicationsInBatch(studentId, position.getBatchId());
        if (batchApplyCount >= 3) {
            throw new BusinessException(40000, "每批次最多报名3个岗位");
        }

        // 7. 生成申请编号
        String applyNo = generateApplyNo();

        // 8. 保存申请信息（严格对齐数据库字段）
        applyInfo.setId(null);
        applyInfo.setDeleted(0);
        applyInfo.setPositionId(positionId);
        applyInfo.setStudentId(studentId);
        applyInfo.setApplyNo(applyNo);
        applyInfo.setStatus(WorkStudyStatus.APPLY_SUBMITTED.getCode()); // 1-已报名
        applyInfo.setInterviewStatus(WorkStudyStatus.INTERVIEW_PENDING.getCode()); // 0-待面试
        applyInfo.setApplyTime(now);
        applyInfo.setCreateTime(now);
        applyInfo.setUpdateTime(now);

        save(applyInfo);

        return applyInfo.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordInterviewResult(Long applyId, Integer interviewStatus) {
        WorkStudyApply apply = getById(applyId);
        if (apply == null || apply.getDeleted() == 1) {
            throw new BusinessException(40400, "报名记录不存在");
        }

        // 只有待面试状态的申请才能录入面试结果
        if (apply.getInterviewStatus() != WorkStudyStatus.INTERVIEW_PENDING.getCode()) {
            throw new BusinessException(40900, "该生面试结果已录入或状态不正确，请勿重复操作");
        }

        // 校验面试状态值是否合法（0-3）
        if (interviewStatus < 0 || interviewStatus > 3) {
            throw new BusinessException(40000, "无效的面试状态");
        }

        apply.setInterviewStatus(interviewStatus);
        apply.setUpdateTime(LocalDateTime.now());

        // 根据面试结果更新申请状态
        if (interviewStatus == WorkStudyStatus.INTERVIEW_PASSED.getCode()) {
            apply.setStatus(WorkStudyStatus.APPLY_PENDING_APPROVAL.getCode()); // 3-待录用审批
        } else if (interviewStatus == WorkStudyStatus.INTERVIEW_FAILED.getCode()) {
            apply.setStatus(WorkStudyStatus.APPLY_REJECTED.getCode()); // 5-未录用
        } else if (interviewStatus == WorkStudyStatus.INTERVIEW_COMPLETED.getCode()) {
            apply.setStatus(WorkStudyStatus.APPLY_INTERVIEWING.getCode()); // 2-面试中
        }

        updateById(apply);
    }

    @Override
    public List<WorkStudyApply> getStudentApplications(Long studentId) {
        return list(new LambdaQueryWrapper<WorkStudyApply>()
                .eq(WorkStudyApply::getStudentId, studentId)
                .eq(WorkStudyApply::getDeleted, 0)
                .orderByDesc(WorkStudyApply::getApplyTime));
    }

    @Override
    public List<WorkStudyApply> getPositionApplications(Long positionId) {
        return list(new LambdaQueryWrapper<WorkStudyApply>()
                .eq(WorkStudyApply::getPositionId, positionId)
                .eq(WorkStudyApply::getDeleted, 0)
                .orderByDesc(WorkStudyApply::getApplyTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTutorRecommendation(Long applyId, String recommendation) {
        WorkStudyApply apply = getById(applyId);
        if (apply == null || apply.getDeleted() == 1) {
            throw new BusinessException(40400, "报名记录不存在");
        }

        // 只有已报名状态的申请才能添加推荐意见
        if (apply.getStatus() != WorkStudyStatus.APPLY_SUBMITTED.getCode()) {
            throw new BusinessException(40900, "当前状态不允许添加推荐意见");
        }

        apply.setTutorRecommend(recommendation);
        apply.setUpdateTime(LocalDateTime.now());
        updateById(apply);
    }

    @Override
    public int countStudentApplicationsInBatch(Long studentId, Long batchId) {
        // 1. 查询该批次下的所有有效岗位ID
        List<Long> positionIds = positionMapper.selectList(
                        new LambdaQueryWrapper<WorkStudyPosition>()
                                .eq(WorkStudyPosition::getBatchId, batchId)
                                .eq(WorkStudyPosition::getDeleted, 0)
                                .select(WorkStudyPosition::getId)
                )
                .stream()
                .map(WorkStudyPosition::getId)
                .toList();

        // 2. 如果没有岗位，直接返回 0
        if (positionIds.isEmpty()) {
            return 0;
        }

        // 3. 统计学生在该批次的报名数
        return Math.toIntExact(count(
                new LambdaQueryWrapper<WorkStudyApply>()
                        .eq(WorkStudyApply::getStudentId, studentId)
                        .eq(WorkStudyApply::getDeleted, 0)
                        .in(WorkStudyApply::getPositionId, positionIds)
        ));
    }

    /**
     * 生成申请编号：WS + 年月日 + 随机字符串
     */
    private String generateApplyNo() {
        String dateStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        String randomStr = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "WS" + dateStr + "-" + randomStr;
    }

}
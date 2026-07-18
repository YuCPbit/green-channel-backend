package edu.workstudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.workstudy.entity.WorkStudyApply;
import edu.workstudy.entity.WorkStudyPosition;
import edu.workstudy.mapper.WorkStudyApplyMapper;
import edu.workstudy.mapper.WorkStudyPositionMapper;
import edu.workstudy.service.WorkStudyApplyService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WorkStudyApplyServiceImpl
        extends ServiceImpl<WorkStudyApplyMapper, WorkStudyApply>
        implements WorkStudyApplyService {

    private final WorkStudyPositionMapper positionMapper;

    public WorkStudyApplyServiceImpl(WorkStudyPositionMapper positionMapper) {
        this.positionMapper = positionMapper;
    }

    @Override
    public Long applyForPosition(Long positionId, Long studentId, WorkStudyApply applyInfo) {
        // 1. 校验岗位是否存在且正在招聘
        WorkStudyPosition position = positionMapper.selectById(positionId);
        if (position == null || position.getStatus() != 2) { // 2=已上架
            throw new RuntimeException("岗位不存在或未开放报名");
        }

        // 2. 校验是否已报名（唯一索引 uk_position_student）
        long count = count(new LambdaQueryWrapper<WorkStudyApply>()
                .eq(WorkStudyApply::getPositionId, positionId)
                .eq(WorkStudyApply::getStudentId, studentId)
                .eq(WorkStudyApply::getIsDeleted, 0));
        if (count > 0) {
            throw new RuntimeException("您已报名该岗位，请勿重复提交");
        }

        // 3. 校验名额（暂时逻辑判断，后续可用乐观锁或分布式锁优化）
        if (position.getHiredCount() >= position.getRecruitCount()) {
            throw new RuntimeException("该岗位招聘名额已满");
        }

        // 4. 生成申请编号（简易版，生产环境需加锁或Redis序列）
        String applyNo = "WS" + System.currentTimeMillis() + studentId;

        applyInfo.setPositionId(positionId);
        applyInfo.setStudentId(studentId);
        applyInfo.setApplyNo(applyNo);
        applyInfo.setStatus(1); // 1-已报名
        applyInfo.setInterviewStatus(0); // 0-待面试
        applyInfo.setApplyTime(LocalDateTime.now());

        save(applyInfo);
        return applyInfo.getId();
    }

    /**
     * 录入面试结果
     * @param applyId 报名ID
     * @param interviewerId 面试官ID（用工部门老师）
     * @param interviewStatus 面试状态: 2-通过 3-不通过
     * @param remark 面试评语
     */

    public void recordInterviewResult(Long applyId, Long interviewerId, Integer interviewStatus, String remark) {
        WorkStudyApply apply = getById(applyId);
        if (apply == null || apply.getIsDeleted() == 1) {
            throw new RuntimeException("报名记录不存在");
        }
        // 只有待面试或已面试未出结果的才能录入
        // 只有“待面试(0)”状态才能录入结果
        if (apply.getInterviewStatus() != 0) {
            throw new RuntimeException("该生面试结果已录入或状态不正确，请勿重复操作");
        }

        apply.setInterviewStatus(interviewStatus); // 2或3
        apply.setInterviewerId(interviewerId);
        apply.setInterviewRemark(remark);
        apply.setInterviewTime(LocalDateTime.now());

        // 如果面试通过，状态变更为待录用审批（3）
        // 如果面试不通过，状态变更为未录用（5）
        if (interviewStatus == 2) {
            apply.setStatus(3); // 待录用审批
        } else if (interviewStatus == 3) {
            apply.setStatus(5); // 未录用
        }

        updateById(apply);
    }
}
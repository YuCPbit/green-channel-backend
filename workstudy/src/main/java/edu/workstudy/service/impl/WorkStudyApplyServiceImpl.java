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
}
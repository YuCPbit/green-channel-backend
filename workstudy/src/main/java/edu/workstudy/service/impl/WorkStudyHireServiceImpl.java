package edu.workstudy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.workstudy.entity.WorkStudyApply;
import edu.workstudy.entity.WorkStudyHire;
import edu.workstudy.entity.WorkStudyPosition;
import edu.workstudy.mapper.WorkStudyHireMapper;
import edu.workstudy.mapper.WorkStudyPositionMapper;
import edu.workstudy.service.WorkStudyApplyService;
import edu.workstudy.service.WorkStudyHireService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class WorkStudyHireServiceImpl
        extends ServiceImpl<WorkStudyHireMapper, WorkStudyHire>
        implements WorkStudyHireService {

    private final WorkStudyApplyService applyService;
    private final WorkStudyPositionMapper positionMapper; // 新增

    // 构造器注入（Lombok的@RequiredArgsConstructor也可）
    public WorkStudyHireServiceImpl(WorkStudyApplyService applyService,
                                    WorkStudyPositionMapper positionMapper) {
        this.applyService = applyService;
        this.positionMapper = positionMapper;
    }

    @Override
    @Transactional // 核心：开启事务，保证数据一致性
    public Long approveHire(Long applyId, Long approverId) {
        // 1. 校验报名记录
        WorkStudyApply apply = applyService.getById(applyId);
        if (apply == null || apply.getStatus() != 3) { // 3=待录用审批
            throw new RuntimeException("申请单状态不正确，无法录用");
        }

        // 2. 查询岗位信息，校验名额
        WorkStudyPosition position = positionMapper.selectById(apply.getPositionId());
        if (position == null) {
            throw new RuntimeException("岗位不存在");
        }
        // 校验：已录用人数 < 招聘人数
        if (position.getHiredCount() >= position.getRecruitCount()) {
            throw new RuntimeException("岗位名额已满，无法继续录用");
        }

        // 3. 创建录用记录
        WorkStudyHire hire = new WorkStudyHire();
        hire.setApplyId(applyId);
        hire.setPositionId(apply.getPositionId());
        hire.setStudentId(apply.getStudentId());
        hire.setHireStatus(1); // 1-在岗
        hire.setHireDate(LocalDate.now());
        hire.setApprovedBy(approverId);
        hire.setApproveTime(LocalDateTime.now());
        save(hire);

        // 4. 更新报名表状态为“已录用”
        apply.setStatus(4); // 4=已录用
        applyService.updateById(apply);

        // 5. 【核心新增】更新岗位的已录用人数 (+1)
        // 使用 UpdateWrapper 进行字段的自增操作，避免并发问题
        boolean updated = updatePositionHiredCount(position.getId());
        if (!updated) {
            throw new RuntimeException("更新岗位录用人数失败，请重试");
        }

        return hire.getId();
    }

    /**
     * 原子性增加岗位已录用人数
     * 使用 setSql 实现 hired_count = hired_count + 1
     */
    private boolean updatePositionHiredCount(Long positionId) {
        return positionMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<WorkStudyPosition>()
                        .eq("id", positionId)
                        // 防止并发超卖：只有当 hired_count < recruit_count 时才更新
                        .lt("hired_count", "recruit_count")
                        .setSql("hired_count = hired_count + 1")) > 0;
    }
}
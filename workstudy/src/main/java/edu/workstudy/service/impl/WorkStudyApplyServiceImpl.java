package edu.workstudy.service.impl; // 确保这里是 impl

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.workstudy.entity.WorkStudyApply;
import edu.workstudy.mapper.WorkStudyApplyMapper;
import edu.workstudy.service.WorkStudyApplyService;
import org.springframework.stereotype.Service;

@Service
public class WorkStudyApplyServiceImpl
        extends ServiceImpl<WorkStudyApplyMapper, WorkStudyApply>
        implements WorkStudyApplyService {

    // 1. 声明为 final，确保不可变
    private final WorkStudyApplyMapper applyMapper;

    // 2. 使用构造器注入（推荐做法）
    public WorkStudyApplyServiceImpl(WorkStudyApplyMapper applyMapper) {
        this.applyMapper = applyMapper;
    }

    @Override
    public String apply(Long positionId, Long studentId) {
        // 业务逻辑示例
        WorkStudyApply apply = new WorkStudyApply();
        apply.setPositionId(positionId);
        apply.setStudentId(studentId);
        // 记得设置其他必填字段，比如状态、创建时间等
        // apply.setStatus(1);
        applyMapper.insert(apply);
        return "WS" + apply.getId();
    }
}
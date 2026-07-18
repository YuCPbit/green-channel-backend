package edu.workstudy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.workstudy.entity.WorkStudyPosition;

public interface WorkStudyPositionService extends IService<WorkStudyPosition> {
    Long publishPosition(WorkStudyPosition position, Long userId);
}
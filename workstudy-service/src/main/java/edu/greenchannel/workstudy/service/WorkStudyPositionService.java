package edu.greenchannel.workstudy.service;

import com.baomidou.mybatisplus.spring.service.IService;
import edu.greenchannel.workstudy.entity.WorkStudyPosition;

public interface WorkStudyPositionService extends IService<WorkStudyPosition> {
    Long publishPosition(WorkStudyPosition position, Long userId);
}
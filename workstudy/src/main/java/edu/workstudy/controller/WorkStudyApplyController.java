package edu.workstudy.controller;

import edu.workstudy.service.WorkStudyApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/applies")
public class WorkStudyApplyController {

    @Autowired
    private WorkStudyApplyService applyService;

    @PostMapping
    public String apply(@RequestParam Long positionId,
                        @RequestHeader("X-User-Id") Long studentId) {
        return applyService.apply(positionId, studentId);
    }
}
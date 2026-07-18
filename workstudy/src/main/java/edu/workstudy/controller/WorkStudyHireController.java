package edu.workstudy.controller;

import edu.workstudy.common.Result;
import edu.workstudy.service.WorkStudyHireService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hire")
public class WorkStudyHireController {

    private final WorkStudyHireService hireService;

    public WorkStudyHireController(WorkStudyHireService hireService) {
        this.hireService = hireService;
    }

    /**
     * 学校审批录用
     */
    @PostMapping("/approve")
    public Result<?> approveHire(@RequestParam Long applyId, @RequestParam Long approverId) {
        Long hireId = hireService.approveHire(applyId, approverId);
        return Result.success(hireId);
    }
}
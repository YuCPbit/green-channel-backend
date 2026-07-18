package edu.workstudy.controller;

import edu.workstudy.common.Result;
import edu.workstudy.entity.WorkStudyBatch;
import edu.workstudy.service.WorkStudyBatchService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workstudy/batch")
public class WorkStudyBatchController {

    private final WorkStudyBatchService batchService;

    public WorkStudyBatchController(WorkStudyBatchService batchService) {
        this.batchService = batchService;
    }

    @PostMapping("/create")
    public Result<Long> createBatch(@RequestBody WorkStudyBatch batch, @RequestParam Long userId) {
        Long batchId = batchService.createBatch(batch, userId);
        return Result.success(batchId);
    }

    @GetMapping("/list")
    public Result<?> listBatches() {
        return Result.success(batchService.list());
    }
}
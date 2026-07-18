package edu.workstudy.controller;

import edu.workstudy.common.Result;
import edu.workstudy.entity.WorkStudyPosition;
import edu.workstudy.service.WorkStudyPositionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workstudy/position")
public class WorkStudyPositionController {

    private final WorkStudyPositionService positionService;

    public WorkStudyPositionController(WorkStudyPositionService positionService) {
        this.positionService = positionService;
    }

    @PostMapping("/publish")
    public Result<Long> publishPosition(@RequestBody WorkStudyPosition position, @RequestParam Long userId) {
        Long positionId = positionService.publishPosition(position, userId);
        return Result.success(positionId);
    }

    @GetMapping("/list")
    public Result<?> listPositions(@RequestParam(required = false) Long batchId) {
        // 简易查询，后续可改为分页
        return Result.success(positionService.lambdaQuery()
                .eq(batchId != null, WorkStudyPosition::getBatchId, batchId)
                .list());
    }
}
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/v1/positions")
public class WorkStudyPositionController {

    @Autowired
    private WorkStudyPositionService positionService;

    @PostMapping
    public Long publish(@RequestParam Long batchId,
                        @RequestBody WorkStudyPosition position,
                        @RequestHeader("X-User-Id") Long userId) {
        return positionService.publish(batchId, position, userId);
    }
}
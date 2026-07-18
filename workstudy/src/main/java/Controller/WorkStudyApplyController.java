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
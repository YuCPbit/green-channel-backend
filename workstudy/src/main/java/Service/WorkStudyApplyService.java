public interface WorkStudyApplyService extends IService<WorkStudyApply> {
    String apply(Long positionId, Long studentId);
}
public interface WorkStudyPositionService extends IService<WorkStudyPosition> {
    Long publish(Long batchId, WorkStudyPosition position, Long userId);
}
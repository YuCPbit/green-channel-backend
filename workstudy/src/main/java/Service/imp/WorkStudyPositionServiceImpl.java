@Service
public class WorkStudyPositionServiceImpl
        extends ServiceImpl<WorkStudyPositionMapper, WorkStudyPosition>
        implements WorkStudyPositionService {

    @Override
    @Transactional
    public Long publish(Long batchId, WorkStudyPosition position, Long userId) {
        position.setBatchId(batchId);
        position.setPublisherId(userId);
        position.setStatus(2); // 已上架
        position.setHiredCount(0);
        save(position);
        return position.getId();
    }
}
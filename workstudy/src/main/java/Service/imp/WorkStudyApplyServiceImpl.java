@Service
public class WorkStudyApplyServiceImpl
        extends ServiceImpl<WorkStudyApplyMapper, WorkStudyApply>
        implements WorkStudyApplyService {

    @Autowired
    private WorkStudyPositionMapper positionMapper;

    @Override
    @Transactional
    public String apply(Long positionId, Long studentId) {

        int count = baseMapper.countByPositionAndStudent(positionId, studentId);
        if (count > 0) {
            throw new RuntimeException("已报名，不可重复");
        }

        WorkStudyPosition position = positionMapper.selectById(positionId);
        if (position.getHiredCount() >= position.getRecruitCount()) {
            throw new RuntimeException("名额已满");
        }

        WorkStudyApply apply = new WorkStudyApply();
        apply.setPositionId(positionId);
        apply.setStudentId(studentId);
        apply.setApplyNo("WS" + System.currentTimeMillis());
        apply.setStatus(1);

        save(apply);

        position.setHiredCount(position.getHiredCount() + 1);
        positionMapper.updateById(position);

        return apply.getApplyNo();
    }
}
package edu.greenchannel.workstudy.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.dto.ActiveHireVO;
import edu.greenchannel.workstudy.dto.PageResult;
import edu.greenchannel.workstudy.dto.WorkStudyEvaluationVO;
import edu.greenchannel.workstudy.entity.WorkStudyEvaluation;
import edu.greenchannel.workstudy.entity.WorkStudyHire;
import edu.greenchannel.workstudy.mapper.WorkStudyEvaluationMapper;
import edu.greenchannel.workstudy.mapper.WorkStudyHireMapper;
import edu.greenchannel.workstudy.service.NotificationService;
import edu.greenchannel.workstudy.service.WorkStudyEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkStudyEvaluationServiceImpl
        extends ServiceImpl<WorkStudyEvaluationMapper, WorkStudyEvaluation>
        implements WorkStudyEvaluationService {

    private final WorkStudyEvaluationMapper evaluationMapper;
    private final WorkStudyHireMapper hireMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitEvaluation(WorkStudyEvaluation evaluation) {
        if (evaluation.getHireId() == null) {
            throw new BusinessException(40000, "录用记录不能为空");
        }
        WorkStudyHire hire = hireMapper.selectById(evaluation.getHireId());
        if (hire == null || hire.getDeleted() == 1 || hire.getHireStatus() != 1) {
            throw new BusinessException(40900, "录用记录不存在或学生已不在岗");
        }
        evaluation.setStudentId(hire.getStudentId());

        long count = count(new LambdaQueryWrapper<WorkStudyEvaluation>()
                .eq(WorkStudyEvaluation::getHireId, evaluation.getHireId())
                .eq(WorkStudyEvaluation::getEvalYear, evaluation.getEvalYear())
                .eq(WorkStudyEvaluation::getEvalMonth, evaluation.getEvalMonth())
                .eq(WorkStudyEvaluation::getDeleted, 0));

        if (count > 0) {
            throw new BusinessException(40900, "该录用记录本月已评价，请勿重复提交");
        }

        if (evaluation.getScore() == null || evaluation.getScore() < 1 || evaluation.getScore() > 5) {
            throw new BusinessException(40000, "评分必须在1-5之间");
        }

        log.info("提交月度评价：hireId={}, studentId={}, 评分={}",
                evaluation.getHireId(), evaluation.getStudentId(), evaluation.getScore());

        evaluation.setEvalTime(LocalDateTime.now());
        evaluation.setDeleted(0);
        try {
            save(evaluation);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(40900, "该录用记录的当月评价已存在，不能重复提交");
        }

        checkWarning(evaluation.getStudentId(), evaluation.getEvalYear(), evaluation.getEvalMonth());
    }

    @Override
    public PageResult<WorkStudyEvaluationVO> listEvaluations(int pageNum, int pageSize,
            Long hireId, Long studentId, Integer evalYear, Integer evalMonth) {
        Page<?> page = new Page<>(pageNum, pageSize);
        QueryWrapper<WorkStudyEvaluationVO> wrapper = new QueryWrapper<>();

        if (hireId != null) {
            wrapper.eq("e.hire_id", hireId);
        }
        if (studentId != null) {
            wrapper.eq("e.student_id", studentId);
        }
        if (evalYear != null) {
            wrapper.eq("e.eval_year", evalYear);
        }
        if (evalMonth != null) {
            wrapper.eq("e.eval_month", evalMonth);
        }

        return PageResult.of(evaluationMapper.selectEvaluationPage(page, wrapper));
    }

    @Override
    public WorkStudyEvaluationVO getDetail(Long id) {
        WorkStudyEvaluationVO vo = evaluationMapper.selectEvaluationById(id);
        if (vo == null) {
            throw new BusinessException(40400, "评价记录不存在");
        }
        return vo;
    }

    @Override
    public void updateEvaluation(Long id, Integer score, String comment, Long operatorId) {
        WorkStudyEvaluation existing = getById(id);
        if (existing == null) {
            throw new BusinessException(40400, "评价记录不存在");
        }

        if (score == null || score < 1 || score > 5) {
            throw new BusinessException(40000, "评分必须在1-5之间");
        }
        if (comment != null && comment.length() > 500) {
            throw new BusinessException(40000, "评语不能超过500字");
        }

        existing.setScore(score);
        existing.setComment(comment);
        updateById(existing);

        log.info("评价已更新：id={}, 新评分={}, 操作人={}", id, score, operatorId);
    }

    @Override
    public void deleteEvaluation(Long id) {
        WorkStudyEvaluation existing = getById(id);
        if (existing == null) {
            throw new BusinessException(40400, "评价记录不存在");
        }
        removeById(id);
        log.info("评价已删除：id={}", id);
    }

    @Override
    public PageResult<WorkStudyEvaluationVO> getMyEvaluations(int pageNum, int pageSize,
            Long studentId, Integer evalYear, Integer evalMonth) {
        return listEvaluations(pageNum, pageSize, null, studentId, evalYear, evalMonth);
    }

    @Override
    public List<ActiveHireVO> getActiveHires(String keyword) {
        return evaluationMapper.selectActiveHires(keyword);
    }

    /**
     * 预警逻辑：检查最近两个月是否都低于2分
     */
    private void checkWarning(Long studentId, int year, int month) {
        // 计算上个月
        int prevMonth = month - 1;
        int prevYear = year;
        if (prevMonth == 0) {
            prevMonth = 12;
            prevYear--;
        }

        // 本月评分
        Integer currentScore = getScore(studentId, year, month);
        // 上月评分
        Integer lastScore = getScore(studentId, prevYear, prevMonth);

        log.debug("预警检查：studentId={}, 本月{}-{}:{}, 上月{}-{}:{}",
                studentId, year, month, currentScore, prevYear, prevMonth, lastScore);

        // 连续两个月低于2分
        if (currentScore != null && currentScore < 2 &&
                lastScore != null && lastScore < 2) {

            String warningContent = String.format(
                    "学生ID:%d 连续两月评价过低（%d年%d月:%d分，%d年%d月:%d分），请关注！",
                    studentId, year, month, currentScore, prevYear, prevMonth, lastScore);

            log.warn("触发评价预警：{}", warningContent);

            notificationService.sendWarningToSchoolAdmins(
                    "勤工助学评价预警",
                    warningContent,
                    studentId.toString()
            );
        }
    }
    /**
     * 查询某学生某月评分
     */
    private Integer getScore(Long studentId, int year, int month) {
        return Optional.ofNullable(getOne(
                new LambdaQueryWrapper<WorkStudyEvaluation>()
                        .eq(WorkStudyEvaluation::getStudentId, studentId)
                        .eq(WorkStudyEvaluation::getEvalYear, year)
                        .eq(WorkStudyEvaluation::getEvalMonth, month)
                        .eq(WorkStudyEvaluation::getDeleted, 0)
                        .select(WorkStudyEvaluation::getScore)
        )).map(WorkStudyEvaluation::getScore).orElse(null);
    }
}

package edu.greenchannel.workstudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.entity.WorkStudyEvaluation;
import edu.greenchannel.workstudy.mapper.WorkStudyEvaluationMapper;
import edu.greenchannel.workstudy.service.NotificationService;
import edu.greenchannel.workstudy.service.WorkStudyEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkStudyEvaluationServiceImpl
        extends ServiceImpl<WorkStudyEvaluationMapper, WorkStudyEvaluation>
        implements WorkStudyEvaluationService {

    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitEvaluation(WorkStudyEvaluation evaluation) {
        // 1. 校验：一个录用记录一个月只能评价一次
        long count = count(new LambdaQueryWrapper<WorkStudyEvaluation>()
                .eq(WorkStudyEvaluation::getHireId, evaluation.getHireId())
                .eq(WorkStudyEvaluation::getEvalYear, evaluation.getEvalYear())
                .eq(WorkStudyEvaluation::getEvalMonth, evaluation.getEvalMonth())
                .eq(WorkStudyEvaluation::getDeleted, 0));

        if (count > 0) {
            throw new BusinessException(40900, "该录用记录本月已评价，请勿重复提交");
        }

        // 2. 校验评分范围
        if (evaluation.getScore() == null || evaluation.getScore() < 1 || evaluation.getScore() > 5) {
            throw new BusinessException(40000, "评分必须在1-5分之间");
        }

        log.info("提交月度评价：hireId={}, studentId={}, 评分={}",
                evaluation.getHireId(), evaluation.getStudentId(), evaluation.getScore());

        evaluation.setEvalTime(LocalDateTime.now());
        evaluation.setDeleted(0);
        save(evaluation);

        // 3. 预警检查
        checkWarning(evaluation.getStudentId(), evaluation.getEvalYear(), evaluation.getEvalMonth());
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

            notificationService.sendWarning(
                    5001L, // 资助中心管理员ID
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
package edu.workstudy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.workstudy.entity.WorkStudyEvaluation;
import edu.workstudy.mapper.WorkStudyEvaluationMapper;
import edu.workstudy.service.NotificationService;
import edu.workstudy.service.WorkStudyEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkStudyEvaluationServiceImpl
        extends ServiceImpl<WorkStudyEvaluationMapper, WorkStudyEvaluation>
        implements WorkStudyEvaluationService {

    private final WorkStudyEvaluationMapper evaluationMapper; // 如果需要直接使用Mapper，可以保留
    private final NotificationService notificationService;

    @Override
    public void submitEvaluation(WorkStudyEvaluation evaluation) {
        log.info("提交月度评价：学生ID={}, 年份={}, 月份={}, 评分={}",
                evaluation.getStudentId(), evaluation.getEvalYear(),
                evaluation.getEvalMonth(), evaluation.getScore());

        evaluation.setEvalTime(LocalDateTime.now());
        save(evaluation);

        // 检查是否需要预警
        checkWarning(evaluation.getStudentId(), evaluation.getEvalYear(), evaluation.getEvalMonth());
    }

    /**
     * 预警逻辑：检查最近两个月是否都低于2分
     */
    private void checkWarning(Long studentId, int year, int month) {
        log.debug("开始检查学生ID={} 的评价预警，年份={}，月份={}", studentId, year, month);

        // 计算上个月
        int prevMonth = month - 1;
        int prevYear = year;
        if (prevMonth == 0) {
            prevMonth = 12;
            prevYear--;
        }

        // 查本月评分
        Integer currentScore = Optional.ofNullable(lambdaQuery()
                        .eq(WorkStudyEvaluation::getStudentId, studentId)
                        .eq(WorkStudyEvaluation::getEvalYear, year)
                        .eq(WorkStudyEvaluation::getEvalMonth, month)
                        .one())
                .map(WorkStudyEvaluation::getScore)
                .orElse(0);

        // 查上月评分
        Integer lastScore = Optional.ofNullable(lambdaQuery()
                        .eq(WorkStudyEvaluation::getStudentId, studentId)
                        .eq(WorkStudyEvaluation::getEvalYear, prevYear)
                        .eq(WorkStudyEvaluation::getEvalMonth, prevMonth)
                        .one())
                .map(WorkStudyEvaluation::getScore)
                .orElse(0);

        log.debug("学生ID={} 本月评分={}，上月评分={}", studentId, currentScore, lastScore);

        // 如果连续两个月低于2分，发预警
        if (currentScore < 2 && lastScore < 2) {
            String warningContent = String.format("学生ID:%d 连续两月评价过低（%d年%d月:%d分，%d年%d月:%d分），请关注！",
                    studentId, year, month, currentScore, prevYear, prevMonth, lastScore);

            log.warn("触发评价预警：{}", warningContent);

            // 调用消息服务发送预警（实际调用的是LocalMockNotificationServiceImpl）
            notificationService.sendWarning(
                    5001L, // 资助中心管理员ID
                    "勤工助学评价预警",
                    warningContent,
                    studentId.toString()
            );
        } else {
            log.debug("学生ID={} 评价正常，无需预警", studentId);
        }
    }
}
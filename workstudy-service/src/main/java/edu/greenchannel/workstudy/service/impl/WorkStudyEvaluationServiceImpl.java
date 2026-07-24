package edu.greenchannel.workstudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.dto.ActiveHireVO;
import edu.greenchannel.workstudy.dto.PageResult;
import edu.greenchannel.workstudy.dto.WorkStudyEvaluationVO;
import edu.greenchannel.workstudy.entity.WorkStudyEvaluation;
import edu.greenchannel.workstudy.entity.WorkStudyHire;
import edu.greenchannel.workstudy.mapper.WorkStudyEvaluationMapper;
import edu.greenchannel.workstudy.service.NotificationService;
import edu.greenchannel.workstudy.service.WorkStudyEvaluationService;
import edu.greenchannel.workstudy.service.WorkStudyHireService;
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
    private final NotificationService notificationService;
    private final WorkStudyHireService hireService;

    @Override
    @Transactional
    public void submitEvaluation(WorkStudyEvaluation evaluation) {
        // 校验评分范围
        if (evaluation.getScore() == null || evaluation.getScore() < 1 || evaluation.getScore() > 5) {
            throw new BusinessException(40000, "评分必须在1-5之间");
        }

        // 校验录用记录存在且有效，且学生与录用记录匹配
        WorkStudyHire hire = hireService.getById(evaluation.getHireId());
        if (hire == null || hire.getIsDeleted() == 1) {
            throw new BusinessException(40400, "录用记录不存在");
        }
        if (hire.getHireStatus() != 1) {
            throw new BusinessException(40900, "该录用记录不在岗，无法评价");
        }
        if (!hire.getStudentId().equals(evaluation.getStudentId())) {
            throw new BusinessException(40300, "学生ID与录用记录不匹配");
        }

        log.info("提交月度评价：hireId={}, 学生ID={}, 年份={}, 月份={}, 评分={}",
                evaluation.getHireId(), evaluation.getStudentId(),
                evaluation.getEvalYear(), evaluation.getEvalMonth(), evaluation.getScore());

        evaluation.setEvalTime(LocalDateTime.now());

        try {
            save(evaluation);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(40900, "该录用记录的当月评价已存在，不能重复提交");
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                throw new BusinessException(40900, "该录用记录的当月评价已存在，不能重复提交");
            }
            throw e;
        }

        // 检查是否需要预警
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
            Integer evalYear, Integer evalMonth, CurrentUser currentUser) {
        // 根据当前登录用户强制确定 studentId，杜绝客户端传参越权
        Long myStudentId = evaluationMapper.findStudentIdByUserId(currentUser.id());
        if (myStudentId == null) {
            throw new BusinessException(40400, "未找到您的学生档案，无法查看评价");
        }
        return listEvaluations(pageNum, pageSize, null, myStudentId, evalYear, evalMonth);
    }

    @Override
    public List<ActiveHireVO> getActiveHires(String keyword) {
        return evaluationMapper.selectActiveHires(keyword);
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

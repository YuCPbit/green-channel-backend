package edu.greenchannel.workstudy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.workstudy.entity.WorkStudyAgreement;
import edu.greenchannel.workstudy.entity.WorkStudyBatch;
import edu.greenchannel.workstudy.entity.WorkStudyHire;
import edu.greenchannel.workstudy.entity.WorkStudyPosition;
import edu.greenchannel.workstudy.mapper.WorkStudyAgreementMapper;
import edu.greenchannel.workstudy.mapper.WorkStudyBatchMapper;
import edu.greenchannel.workstudy.mapper.WorkStudyPositionMapper;
import edu.greenchannel.workstudy.service.WorkStudyAgreementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkStudyAgreementServiceImpl
        extends ServiceImpl<WorkStudyAgreementMapper, WorkStudyAgreement>
        implements WorkStudyAgreementService {

    private final WorkStudyAgreementMapper agreementMapper;
    private final WorkStudyPositionMapper positionMapper;
    private final WorkStudyBatchMapper batchMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateAgreement(WorkStudyHire hire) {
        // 1. 校验：同一录用记录只能生成一个协议
        long count = agreementMapper.selectCount(new LambdaQueryWrapper<WorkStudyAgreement>()
                .eq(WorkStudyAgreement::getHireId, hire.getId())
                .eq(WorkStudyAgreement::getDeleted, 0));

        if (count > 0) {
            log.warn("录用记录 {} 已生成协议，跳过重复生成", hire.getId());
            return;
        }

        // 2. 获取岗位和批次信息
        WorkStudyPosition position = positionMapper.selectById(hire.getPositionId());
        if (position == null) {
            throw new BusinessException(40000, "岗位不存在");
        }

        WorkStudyBatch batch = batchMapper.selectById(position.getBatchId());
        if (batch == null) {
            throw new BusinessException(40000, "批次不存在");
        }

        // 3. 生成唯一协议编号
        String agreementNo = generateAgreementNo(hire.getId());

        // 4. 生成协议内容
        String templateContent = generateTemplateContent(hire, position, batch, agreementNo);

        // 5. 创建协议记录
        WorkStudyAgreement agreement = new WorkStudyAgreement();
        agreement.setHireId(hire.getId());
        agreement.setStudentId(hire.getStudentId());
        agreement.setPositionId(hire.getPositionId());
        agreement.setAgreementNo(agreementNo);
        agreement.setTemplateContent(templateContent);
        agreement.setStartDate(batch.getWorkStartDate()); // 使用批次开始日期
        agreement.setEndDate(batch.getWorkEndDate());     // 使用批次结束日期
        agreement.setSignStatus(0); // 0-待签署
        agreement.setRenewCount(0);
        agreement.setDeleted(0);
        agreement.setCreateTime(LocalDateTime.now());
        agreement.setUpdateTime(LocalDateTime.now());

        agreementMapper.insert(agreement);
        /*
        messagePublisher.publish(new MessageEvent(
                MessageEventType.WORK_STUDY_AGREEMENT_PENDING.name(),
                agreement.getStudentId(),
                agreement.getId().toString(),
                Map.of(
                        "agreementNo", agreement.getAgreementNo(),
                        "positionName", position.getPositionName()
                )
        ));
        */
        log.info("协议生成成功：agreementNo={}, hireId={}", agreementNo, hire.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void signAgreement(Long agreementId, Long studentId) {
        WorkStudyAgreement agreement = agreementMapper.selectById(agreementId);
        if (agreement == null || Objects.equals(agreement.getDeleted(), 1)) {
            throw new BusinessException(40400, "协议不存在");
        }

        // 1. 权限校验
        if (!agreement.getStudentId().equals(studentId)) {
            throw new BusinessException(40300, "无权签署该协议");
        }

        // 2. 状态校验
        if (!Objects.equals(agreement.getSignStatus(), 0)) {
            throw new BusinessException(40000, "协议已签署或已过期");
        }

        // 3. 时间校验（协议必须在有效期内签署）
        LocalDate today = LocalDate.now();
        if (today.isBefore(agreement.getStartDate()) || today.isAfter(agreement.getEndDate())) {
            throw new BusinessException(40000, "协议已过签署有效期");
        }

        agreement.setSignStatus(1); // 1-已签署
        agreement.setStudentSignTime(LocalDateTime.now());
        agreement.setUpdateTime(LocalDateTime.now());

        agreementMapper.updateById(agreement);
        log.info("学生签署协议成功：agreementId={}, studentId={}", agreementId, studentId);
    }

    /**
     * 续签协议
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void renewAgreement(Long agreementId, Long operatorId, LocalDate newEndDate) {
        WorkStudyAgreement agreement = agreementMapper.selectById(agreementId);
        if (agreement == null || Objects.equals(agreement.getDeleted(), 1)) {
            throw new BusinessException(40400, "协议不存在");
        }

        if (!Objects.equals(agreement.getSignStatus(), 1)
                && !Objects.equals(agreement.getSignStatus(), 3)) {
            throw new BusinessException(40000, "协议状态不允许续签");
        }

        LocalDate currentEndDate = agreement.getEndDate();
        if (currentEndDate == null) {
            throw new BusinessException(40900, "协议缺少到期日期，无法续签");
        }
        LocalDate targetEndDate = newEndDate == null ? currentEndDate.plusMonths(6) : newEndDate;
        if (!targetEndDate.isAfter(currentEndDate)) {
            throw new BusinessException(40000, "续签后的到期日期必须晚于原到期日期");
        }

        int currentRenewCount = agreement.getRenewCount() == null ? 0 : agreement.getRenewCount();
        LambdaUpdateWrapper<WorkStudyAgreement> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WorkStudyAgreement::getId, agreementId)
                .eq(WorkStudyAgreement::getRenewCount, currentRenewCount)
                .in(WorkStudyAgreement::getSignStatus, 1, 3)
                .eq(WorkStudyAgreement::getDeleted, 0)
                .set(WorkStudyAgreement::getEndDate, targetEndDate)
                .set(WorkStudyAgreement::getRenewCount, currentRenewCount + 1)
                .set(WorkStudyAgreement::getSignStatus, 3)
                .set(WorkStudyAgreement::getUpdateTime, LocalDateTime.now());
        if (agreementMapper.update(null, updateWrapper) != 1) {
            throw new BusinessException(40900, "协议状态已变化，请刷新后重试");
        }
        log.info("协议续签成功：agreementId={}, operatorId={}, renewCount={}, endDate={}",
                agreementId, operatorId, currentRenewCount + 1, targetEndDate);
    }

    @Override
    public WorkStudyAgreement getAccessibleAgreement(Long agreementId, Long userId, Integer userType) {
        WorkStudyAgreement agreement = agreementMapper.selectById(agreementId);
        if (agreement == null || Objects.equals(agreement.getDeleted(), 1)) {
            throw new BusinessException(40400, "协议不存在");
        }
        if (Objects.equals(userType, 1) && !Objects.equals(agreement.getStudentId(), userId)) {
            throw new BusinessException(40300, "无权查看该协议");
        }
        return agreement;
    }

    @Override
    public List<WorkStudyAgreement> listAgreements(Long studentId, Integer signStatus) {
        LambdaQueryWrapper<WorkStudyAgreement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WorkStudyAgreement::getDeleted, 0)
                .eq(studentId != null, WorkStudyAgreement::getStudentId, studentId)
                .eq(signStatus != null, WorkStudyAgreement::getSignStatus, signStatus)
                .orderByAsc(WorkStudyAgreement::getEndDate)
                .orderByDesc(WorkStudyAgreement::getCreateTime);
        return agreementMapper.selectList(queryWrapper);
    }

    /**
     * 生成协议编号
     * 规则：GC + 年月日 + 6位随机数
     */
    private String generateAgreementNo(Long hireId) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String randomStr = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "GC" + dateStr + randomStr;
    }

    /**
     * 生成协议模板内容
     */
    private String generateTemplateContent(
            WorkStudyHire hire,
            WorkStudyPosition position,
            WorkStudyBatch batch,
            String agreementNo) {
        return String.format("""
                # 勤工助学协议书

                ## 基本信息
                - 协议编号：%s
                - 学生ID：%d
                - 岗位名称：%s
                - 用工部门：%s
                - 薪酬标准：%.2f 元/小时

                ## 协议期限
                - 开始日期：%s
                - 结束日期：%s

                ## 工作内容
                %s

                ## 工作要求
                %s

                ---
                生成时间：%s
                """,
                agreementNo,
                hire.getStudentId(),
                position.getPositionName(),
                position.getDepartmentName(),
                position.getSalaryRate(),
                batch.getWorkStartDate(),
                batch.getWorkEndDate(),
                position.getDescription() != null ? position.getDescription() : "无",
                position.getRequirements() != null ? position.getRequirements() : "无",
                LocalDateTime.now()
        );
    }
}

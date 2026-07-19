package edu.workstudy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.workstudy.entity.WorkStudyAgreement;
import edu.workstudy.mapper.WorkStudyAgreementMapper;
import edu.workstudy.service.WorkStudyAgreementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkStudyAgreementServiceImpl
        extends ServiceImpl<WorkStudyAgreementMapper, WorkStudyAgreement>
        implements WorkStudyAgreementService {

    private final WorkStudyAgreementMapper agreementMapper;

    @Override
    @Transactional
    public void generateAgreement(Long hireId) {
        // ✅ 不再查 hire 表，直接生成协议
        // hireId 已经足够作为业务主键
        String agreementNo = "GC" + LocalDate.now()
                .format(DateTimeFormatter.BASIC_ISO_DATE)
                + String.format("%06d", hireId);

        WorkStudyAgreement agreement = new WorkStudyAgreement();
        agreement.setHireId(hireId);
        agreement.setAgreementNo(agreementNo);
        agreement.setTemplateContent("<h1>勤工助学协议书</h1>");
        agreement.setStartDate(LocalDate.now());
        agreement.setEndDate(LocalDate.now().plusMonths(4));
        agreement.setSignStatus(0);
        agreement.setRenewCount(0);

        save(agreement);
        log.info("协议生成成功，agreementNo={}", agreementNo);
    }

    @Override
    @Transactional
    public void signAgreement(Long agreementId, Long studentId) {
        WorkStudyAgreement agreement = getById(agreementId);
        if (agreement == null) {
            throw new RuntimeException("协议不存在");
        }
        if (!agreement.getStudentId().equals(studentId)) {
            throw new RuntimeException("无权签署该协议");
        }
        if (agreement.getSignStatus() != 0) {
            throw new RuntimeException("协议已签署或已过期");
        }

        agreement.setSignStatus(1);
        agreement.setStudentSignTime(LocalDateTime.now());
        updateById(agreement);

        log.info("学生签署协议成功，agreementId={}, studentId={}", agreementId, studentId);
    }
}
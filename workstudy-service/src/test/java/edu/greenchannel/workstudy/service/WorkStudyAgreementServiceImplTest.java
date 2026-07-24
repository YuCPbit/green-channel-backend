package edu.greenchannel.workstudy.service;

import edu.greenchannel.workstudy.entity.WorkStudyAgreement;
import edu.greenchannel.workstudy.entity.WorkStudyBatch;
import edu.greenchannel.workstudy.entity.WorkStudyHire;
import edu.greenchannel.workstudy.entity.WorkStudyPosition;
import edu.greenchannel.workstudy.mapper.WorkStudyAgreementMapper;
import edu.greenchannel.workstudy.mapper.WorkStudyBatchMapper;
import edu.greenchannel.workstudy.mapper.WorkStudyPositionMapper;
import edu.greenchannel.workstudy.service.impl.WorkStudyAgreementServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkStudyAgreementServiceImplTest {

    @Mock
    private WorkStudyAgreementMapper agreementMapper;

    @Mock
    private WorkStudyPositionMapper positionMapper;

    @Mock
    private WorkStudyBatchMapper batchMapper;

    @InjectMocks
    private WorkStudyAgreementServiceImpl service;

    @Test
    void generatedAgreementKeepsStudentAndPositionOwnership() {
        // 准备 Hire 数据
        WorkStudyHire hire = new WorkStudyHire();
        hire.setId(12L);
        hire.setStudentId(77L);
        hire.setPositionId(8L);

        // Mock 依赖行为（防止 NPE）
        WorkStudyPosition position = new WorkStudyPosition();
        position.setBatchId(1L);
        when(positionMapper.selectById(8L)).thenReturn(position);

        WorkStudyBatch batch = new WorkStudyBatch();
        batch.setWorkStartDate(LocalDate.now());
        batch.setWorkEndDate(LocalDate.now().plusMonths(4));
        when(batchMapper.selectById(1L)).thenReturn(batch);

        // 执行测试
        service.generateAgreement(hire);

        // 验证结果
        ArgumentCaptor<WorkStudyAgreement> captor = ArgumentCaptor.forClass(WorkStudyAgreement.class);
        verify(agreementMapper).insert(captor.capture());

        assertEquals(12L, captor.getValue().getHireId());
        assertEquals(77L, captor.getValue().getStudentId());
        assertEquals(8L, captor.getValue().getPositionId());
    }
}
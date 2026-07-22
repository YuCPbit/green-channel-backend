package edu.greenchannel.workstudy.service;

import edu.greenchannel.workstudy.entity.WorkStudyAgreement;
import edu.greenchannel.workstudy.entity.WorkStudyHire;
import edu.greenchannel.workstudy.mapper.WorkStudyAgreementMapper;
import edu.greenchannel.workstudy.service.impl.WorkStudyAgreementServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WorkStudyAgreementServiceImplTest {

    @Test
    void generatedAgreementKeepsStudentAndPositionOwnership() {
        WorkStudyAgreementMapper mapper = mock(WorkStudyAgreementMapper.class);
        WorkStudyAgreementServiceImpl service = new WorkStudyAgreementServiceImpl(mapper);
        WorkStudyHire hire = new WorkStudyHire();
        hire.setId(12L);
        hire.setStudentId(77L);
        hire.setPositionId(8L);

        service.generateAgreement(hire);

        ArgumentCaptor<WorkStudyAgreement> captor = ArgumentCaptor.forClass(WorkStudyAgreement.class);
        verify(mapper).insert(captor.capture());
        assertEquals(12L, captor.getValue().getHireId());
        assertEquals(77L, captor.getValue().getStudentId());
        assertEquals(8L, captor.getValue().getPositionId());
    }
}

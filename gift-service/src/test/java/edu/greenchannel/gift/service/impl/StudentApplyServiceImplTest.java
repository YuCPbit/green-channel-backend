package edu.greenchannel.gift.service.impl;

import edu.greenchannel.common.BusinessException;
import edu.greenchannel.gift.entity.GiftPackBatch;
import edu.greenchannel.gift.entity.StudentApply;
import edu.greenchannel.gift.mapper.GiftPackBatchMapper;
import edu.greenchannel.gift.mapper.StudentApplyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentApplyServiceImplTest {

    @Mock
    private StudentApplyMapper studentApplyMapper;

    @Mock
    private GiftPackBatchMapper giftPackBatchMapper;

    private StudentApplyServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new StudentApplyServiceImpl(giftPackBatchMapper);
        ReflectionTestUtils.setField(service, "baseMapper", studentApplyMapper);
    }

    @Test
    void submitIgnoresClientIdentityAndWorkflowFields() {
        GiftPackBatch batch = new GiftPackBatch();
        batch.setId(3L);
        batch.setStatus(1);
        batch.setIsDeleted(0);
        when(giftPackBatchMapper.selectById(3L)).thenReturn(batch);

        StudentApply request = new StudentApply();
        request.setPackBatchId(3L);
        request.setStudentId(999L);
        request.setStatus(5);
        request.setPickupCode("CLIENT-CODE");
        request.setApplyReason(" 家庭困难 ");

        service.submit(request, 77L);

        ArgumentCaptor<StudentApply> captor = ArgumentCaptor.forClass(StudentApply.class);
        verify(studentApplyMapper).insert(captor.capture());
        StudentApply saved = captor.getValue();
        assertEquals(77L, saved.getStudentId());
        assertEquals(2, saved.getStatus());
        assertEquals(0, saved.getPickupStatus());
        assertEquals("家庭困难", saved.getApplyReason());
        assertNull(saved.getPickupCode());
    }

    @Test
    void studentCannotReadAnotherStudentsApplication() {
        StudentApply apply = new StudentApply();
        apply.setId(8L);
        apply.setStudentId(99L);
        apply.setIsDeleted(0);
        when(studentApplyMapper.selectById(8L)).thenReturn(apply);

        BusinessException exception = assertThrows(
                BusinessException.class, () -> service.getMine(8L, 77L));

        assertEquals(40300, exception.getCode());
    }
}

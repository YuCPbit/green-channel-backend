package edu.greenchannel.gift.service.impl.review;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import edu.greenchannel.auth.TokenService;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.gift.dto.review.GiftPickupDTO;
import edu.greenchannel.gift.entity.StudentApply;
import edu.greenchannel.gift.mapper.StudentApplyMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GiftReviewServiceImplTest {

    @Mock
    private StudentApplyMapper studentApplyMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private TokenService tokenService;

    private GiftReviewServiceImpl service;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), "gift-review-test"),
                StudentApply.class);
        service = new GiftReviewServiceImpl(studentApplyMapper, request, tokenService);
        when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        when(tokenService.resolve("test-token")).thenReturn(Optional.of(new CurrentUser(
                88L, "school01", "测试资助中心", 4, "学校资助中心",
                List.of("SCHOOL_ADMIN"), List.of("gift:pickup:manage"), List.of("礼包核销管理"))));
    }

    @Test
    void pickupTrimsCodeAndMarksPendingApplication() {
        when(studentApplyMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(application(5, 0));
        when(studentApplyMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        service.pickup(pickup(" CODE-001 ", 88L, " 现场领取 "));

        ArgumentCaptor<LambdaQueryWrapper<StudentApply>> queryCaptor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(studentApplyMapper).selectOne(queryCaptor.capture());
        queryCaptor.getValue().getSqlSegment();
        Collection<Object> values = queryCaptor.getValue().getParamNameValuePairs().values();
        assertTrue(values.contains("CODE-001"));

        verify(studentApplyMapper).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void pickupRejectsRepeatedOperation() {
        when(studentApplyMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(application(5, 1));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.pickup(pickup("CODE-001", 88L, null)));

        assertEquals(40900, exception.getCode());
        verify(studentApplyMapper, never()).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void pickupRejectsUnknownCode() {
        when(studentApplyMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.pickup(pickup("UNKNOWN", 88L, null)));

        assertEquals(40400, exception.getCode());
    }

    @Test
    void pickupRejectsConcurrentSecondUpdate() {
        when(studentApplyMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(application(5, 0));
        when(studentApplyMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.pickup(pickup("CODE-001", 88L, null)));

        assertEquals(40900, exception.getCode());
    }

    @Test
    void exceptionRegistrationRequiresRemark() {
        when(studentApplyMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(application(5, 0));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.pickupException(pickup("CODE-001", 88L, " ")));

        assertEquals(40000, exception.getCode());
        verify(studentApplyMapper, never()).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    private static StudentApply application(int status, int pickupStatus) {
        StudentApply apply = new StudentApply();
        apply.setId(1L);
        apply.setPickupCode("CODE-001");
        apply.setStatus(status);
        apply.setPickupStatus(pickupStatus);
        apply.setIsDeleted(0);
        return apply;
    }

    private static GiftPickupDTO pickup(String code, Long operatorId, String remark) {
        GiftPickupDTO dto = new GiftPickupDTO();
        dto.setPickupCode(code);
        dto.setOperatorId(operatorId);
        dto.setRemark(remark);
        return dto;
    }
}

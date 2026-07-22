package edu.greenchannel.dashboard.service.modules;

import edu.greenchannel.common.BusinessException;
import edu.greenchannel.dashboard.common.FieldWhiteList;
import edu.greenchannel.dashboard.domain.dto.CustomReportReqDTO;
import edu.greenchannel.dashboard.mapper.DashboardMapper;
import edu.greenchannel.dashboard.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
class WorkStudyModuleDataServiceTest {
    private MapperStub mapperStub;
    private WorkStudyModuleDataService service;

    @BeforeEach
    void setUp() {
        mapperStub = new MapperStub();
        DashboardMapper mapper = proxy(DashboardMapper.class, mapperStub);
        DashboardService dashboardService = proxy(DashboardService.class, (proxy, method, args) -> null);
        service = new WorkStudyModuleDataService(dashboardService, mapper, new FieldWhiteList());
    }

    @Test
    void parsesFiltersBeforePassingThemToParameterizedMapper() {
        CustomReportReqDTO request = request("position-stat", Map.of("batchId", "12"));
        mapperStub.positionRows = List.of(Map.of(
                "dept_name", "图书馆", "position_count", 2, "plan_recruit_count", 8,
                "apply_count", 10, "hire_count", 6, "on_job_count", 5, "on_job_rate", 62.5));

        List<Map<String, Object>> rows = service.getReportData(request);

        assertEquals(12L, mapperStub.batchId);
        assertEquals("图书馆", rows.get(0).get("dept_name"));
        assertEquals(List.of("dept_name", "position_count", "plan_recruit_count", "apply_count",
                "hire_count", "on_job_count", "on_job_rate"), rows.get(0).keySet().stream().toList());
    }

    @Test
    void rejectsSqlLikeFilterInsteadOfConcatenatingIt() {
        CustomReportReqDTO request = request("position-stat", Map.of("batchId", "1 OR 1=1"));

        assertThrows(BusinessException.class, () -> service.getReportData(request));
        assertEquals(0, mapperStub.invocations);
    }

    private CustomReportReqDTO request(String reportType, Map<String, Object> filters) {
        CustomReportReqDTO request = new CustomReportReqDTO();
        request.setModule("workstudy");
        request.setReportType(reportType);
        request.setFilters(filters);
        return request;
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static class MapperStub implements java.lang.reflect.InvocationHandler {
        private int invocations;
        private Long batchId;
        private List<Map<String, Object>> positionRows = List.of();

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> "MapperStub";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                };
            }
            invocations++;
            if (method.getName().equals("selectWsPositionReport")) {
                batchId = (Long) args[0];
                return positionRows;
            }
            return null;
        }
    }
}

package edu.greenchannel.dashboard.common;

import edu.greenchannel.common.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FieldWhiteListTest {

    private final FieldWhiteList fieldWhiteList = new FieldWhiteList();

    @Test
    void mapsAllowedReportFields() {
        assertEquals(List.of("s.student_no", "c.college_name"),
                fieldWhiteList.validateAndGetColumns(List.of("student_no", "college_name")));
    }

    @Test
    void rejectsRawSqlOrUnknownFields() {
        assertThrows(BusinessException.class,
                () -> fieldWhiteList.validateAndGetColumns(List.of("student_no", "s.password")));
    }

    @Test
    void defaultsAndProjectsWorkStudyReportFields() {
        List<String> fields = fieldWhiteList.validateReportFields("workstudy", "student-stat", null);
        List<Map<String, Object>> rows = fieldWhiteList.projectRows(
                List.of(Map.of("COLLEGE_NAME", "计算机学院", "STUDENT_COUNT", 6,
                        "POVERTY_LEVEL_TEXT", "困难")), fields);

        assertEquals(List.of("college_name", "poverty_level_text", "student_count"), fields);
        assertEquals("计算机学院", rows.get(0).get("college_name"));
        assertEquals(6, rows.get(0).get("student_count"));
    }

    @Test
    void rejectsCrossModuleOrInjectedReportFields() {
        assertThrows(BusinessException.class, () -> fieldWhiteList.validateReportFields(
                "workstudy", "position-stat", List.of("dept_name", "s.password")));
    }
}

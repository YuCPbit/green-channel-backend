package edu.greenchannel.dashboard.common;

import org.junit.jupiter.api.Test;

import java.util.List;

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
        assertThrows(IllegalArgumentException.class,
                () -> fieldWhiteList.validateAndGetColumns(List.of("student_no", "s.password")));
    }
}

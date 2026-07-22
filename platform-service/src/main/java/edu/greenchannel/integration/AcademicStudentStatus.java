package edu.greenchannel.integration;

public record AcademicStudentStatus(
        String studentNo,
        String academicStatus,
        boolean registrationLocked,
        String source) {
}

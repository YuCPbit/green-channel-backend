package edu.greenchannel.student;

public record StudentRequest(
        String studentNo,
        String name,
        Integer gender,
        String idCard,
        String phone,
        String email,
        Integer enrollYear,
        Long collegeId,
        Long majorId,
        Long classId,
        String studentType) {
}

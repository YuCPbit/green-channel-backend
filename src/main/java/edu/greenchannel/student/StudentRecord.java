package edu.greenchannel.student;

public record StudentRecord(
        long id,
        long userId,
        String studentNo,
        String name,
        Integer gender,
        String protectedIdCard,
        String phone,
        String email,
        int enrollYear,
        long collegeId,
        long majorId,
        Long classId,
        String studentType,
        boolean infoCompleted) {
}

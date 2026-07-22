package edu.greenchannel.student;

public record StudentView(
        long id,
        String studentNo,
        String name,
        Integer gender,
        String idCardStatus,
        String maskedPhone,
        String email,
        int enrollYear,
        long collegeId,
        long majorId,
        Long classId,
        String studentType,
        boolean infoCompleted) {
    static StudentView from(StudentRecord record) {
        return new StudentView(
                record.id(), record.studentNo(), record.name(), record.gender(), "PROTECTED",
                maskPhone(record.phone()), record.email(), record.enrollYear(), record.collegeId(),
                record.majorId(), record.classId(), record.studentType(), record.infoCompleted());
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}

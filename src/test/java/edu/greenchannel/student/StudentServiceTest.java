package edu.greenchannel.student;

import edu.greenchannel.common.BusinessException;
import edu.greenchannel.common.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentServiceTest {
    private final InMemoryRepository repository = new InMemoryRepository();
    private final StudentExcelService excelService = new StudentExcelService();
    private final StudentService service = new StudentService(
            repository, new StudentDataProtector(), excelService);

    @Test
    void importsStandardExcelAndProtectsSensitiveFields() {
        byte[] template = excelService.template();
        MockMultipartFile file = new MockMultipartFile(
                "file", "students.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", template);

        StudentImportResult result = service.importExcel(file);

        assertEquals(1, result.importedRows());
        assertEquals(0, result.failedRows());
        assertTrue(repository.values.get(0).protectedIdCard().startsWith("sha256$"));
        assertFalse(repository.values.get(0).protectedIdCard().contains("110101"));
    }

    @Test
    void rejectsDuplicateStudentNumber() {
        StudentRequest request = validRequest();
        service.create(request);

        BusinessException error = assertThrows(BusinessException.class, () -> service.create(request));

        assertEquals(40900, error.getCode());
    }

    @Test
    void preventsCriticalChangeAfterApplication() {
        StudentView created = service.create(validRequest());
        repository.hasApplication = true;
        StudentRequest changed = new StudentRequest(
                "20260002", "示例学生", 2, "000000200001010021", "13800000000",
                "student@example.invalid", 2026, 2L, 1L, 1L, "本科");

        BusinessException error = assertThrows(
                BusinessException.class, () -> service.update(created.id(), changed));

        assertEquals(40900, error.getCode());
    }

    private StudentRequest validRequest() {
        return new StudentRequest(
                "20260001", "示例学生", 2, "000000200001010021", "13800000000",
                "student@example.invalid", 2026, 1L, 1L, 1L, "本科");
    }

    private static class InMemoryRepository implements StudentRepository {
        private final List<StudentRecord> values = new ArrayList<>();
        private boolean hasApplication;

        @Override
        public boolean existsByStudentNo(String studentNo, Long excludedId) {
            return values.stream().anyMatch(value -> value.studentNo().equals(studentNo)
                    && (excludedId == null || value.id() != excludedId));
        }

        @Override
        public StudentRecord insert(StudentRecord student, String passwordHash) {
            StudentRecord saved = new StudentRecord(
                    values.size() + 1L, values.size() + 11L, student.studentNo(), student.name(),
                    student.gender(), student.protectedIdCard(), student.phone(), student.email(),
                    student.enrollYear(), student.collegeId(), student.majorId(), student.classId(),
                    student.studentType(), student.infoCompleted());
            values.add(saved);
            return saved;
        }

        @Override
        public Optional<StudentRecord> findById(long id) {
            return values.stream().filter(value -> value.id() == id).findFirst();
        }

        @Override
        public StudentRecord update(StudentRecord student) {
            values.replaceAll(value -> value.id() == student.id() ? student : value);
            return student;
        }

        @Override
        public PageResult<StudentRecord> search(
                String studentNo, String name, Long collegeId, Integer enrollYear, int page, int size) {
            return new PageResult<>(List.copyOf(values), values.size(), page, size);
        }

        @Override
        public boolean hasBusinessApplication(long studentId) {
            return hasApplication;
        }

        @Override
        public void softDelete(long studentId, long userId) {
            values.removeIf(value -> value.id() == studentId);
        }
    }
}

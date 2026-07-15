package edu.greenchannel.student;

import edu.greenchannel.common.PageResult;

import java.util.Optional;

public interface StudentRepository {
    boolean existsByStudentNo(String studentNo, Long excludedId);

    StudentRecord insert(StudentRecord student, String passwordHash);

    Optional<StudentRecord> findById(long id);

    StudentRecord update(StudentRecord student);

    PageResult<StudentRecord> search(
            String studentNo, String name, Long collegeId, Integer enrollYear, int page, int size);

    boolean hasBusinessApplication(long studentId);

    void softDelete(long studentId, long userId);
}

package edu.greenchannel.student;

import edu.greenchannel.common.BusinessException;
import edu.greenchannel.common.PageResult;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class StudentService {
    private static final Pattern STUDENT_NO = Pattern.compile("[A-Za-z0-9_-]{4,30}");
    private static final Pattern PHONE = Pattern.compile("1[3-9]\\d{9}");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int[] ID_WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] ID_CHECK = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    private final StudentRepository repository;
    private final StudentDataProtector protector;
    private final StudentExcelService excelService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public StudentService(
            StudentRepository repository, StudentDataProtector protector, StudentExcelService excelService) {
        this.repository = repository;
        this.protector = protector;
        this.excelService = excelService;
    }

    public byte[] importTemplate() {
        return excelService.template();
    }

    @Transactional
    public StudentImportResult importExcel(MultipartFile file) {
        List<StudentExcelService.ImportedRow> rows = excelService.read(file);
        List<StudentImportError> errors = new ArrayList<>();
        Set<String> numbersInFile = new HashSet<>();
        int imported = 0;
        for (StudentExcelService.ImportedRow row : rows) {
            if (row.parseError() != null) {
                errors.add(new StudentImportError(row.rowNumber(), "", row.parseError()));
                continue;
            }
            try {
                StudentRequest normalized = normalizeAndValidate(row.request(), true);
                if (!numbersInFile.add(normalized.studentNo())) {
                    throw new BusinessException(40900, "文件内学号重复");
                }
                if (repository.existsByStudentNo(normalized.studentNo(), null)) {
                    throw new BusinessException(40900, "学号已存在");
                }
                insert(normalized);
                imported++;
            } catch (BusinessException exception) {
                errors.add(new StudentImportError(
                        row.rowNumber(), safeStudentNo(row.request().studentNo()), exception.getMessage()));
            }
        }
        return new StudentImportResult(rows.size(), imported, errors.size(), true, List.copyOf(errors));
    }

    @Transactional
    public StudentView create(StudentRequest request) {
        StudentRequest normalized = normalizeAndValidate(request, true);
        if (repository.existsByStudentNo(normalized.studentNo(), null)) {
            throw new BusinessException(40900, "学号已存在");
        }
        return StudentView.from(insert(normalized));
    }

    public PageResult<StudentView> search(
            String studentNo, String name, Long collegeId, Integer enrollYear, int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BusinessException(40001, "分页参数不正确");
        }
        PageResult<StudentRecord> result = repository.search(
                trimToNull(studentNo), trimToNull(name), collegeId, enrollYear, page, size);
        return new PageResult<>(result.items().stream().map(StudentView::from).toList(),
                result.total(), result.page(), result.size());
    }

    @Transactional
    public StudentView update(long id, StudentRequest request) {
        StudentRecord existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40400, "学生不存在"));
        StudentRequest normalized = normalizeAndValidate(request, false);
        if (repository.existsByStudentNo(normalized.studentNo(), id)) {
            throw new BusinessException(40900, "学号已存在");
        }
        boolean criticalChanged = !existing.studentNo().equals(normalized.studentNo())
                || existing.collegeId() != normalized.collegeId();
        if (criticalChanged && repository.hasBusinessApplication(id)) {
            throw new BusinessException(40900, "学生已有资助申请，不能修改学号或学院");
        }
        String protectedId = StringUtils.hasText(request.idCard())
                ? protector.protect(normalized.idCard()) : existing.protectedIdCard();
        StudentRecord updated = new StudentRecord(
                id, existing.userId(), normalized.studentNo(), normalized.name(), normalized.gender(),
                protectedId, normalized.phone(), normalized.email(), normalized.enrollYear(),
                normalized.collegeId(), normalized.majorId(), normalized.classId(),
                normalized.studentType(), true);
        return StudentView.from(repository.update(updated));
    }

    @Transactional
    public void delete(long id) {
        StudentRecord existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40400, "学生不存在"));
        if (repository.hasBusinessApplication(id)) {
            throw new BusinessException(40900, "学生已有资助申请，不能删除");
        }
        repository.softDelete(id, existing.userId());
    }

    @Transactional
    public int batchDelete(StudentBatchDeleteRequest request) {
        if (request == null || request.ids() == null || request.ids().isEmpty() || request.ids().size() > 100) {
            throw new BusinessException(40001, "请选择 1 至 100 条学生记录");
        }
        int deleted = 0;
        for (Long id : request.ids().stream().distinct().toList()) {
            if (id == null) {
                throw new BusinessException(40001, "学生ID不能为空");
            }
            delete(id);
            deleted++;
        }
        return deleted;
    }

    private StudentRecord insert(StudentRequest value) {
        StudentRecord student = new StudentRecord(
                0, 0, value.studentNo(), value.name(), value.gender(), protector.protect(value.idCard()),
                value.phone(), value.email(), value.enrollYear(), value.collegeId(), value.majorId(),
                value.classId(), value.studentType(), true);
        String randomPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
        return repository.insert(student, randomPasswordHash);
    }

    private StudentRequest normalizeAndValidate(StudentRequest request, boolean requireIdCard) {
        if (request == null) {
            throw new BusinessException(40001, "学生信息不能为空");
        }
        String studentNo = required(request.studentNo(), "学号").toUpperCase(Locale.ROOT);
        String name = required(request.name(), "姓名");
        String idCard = requireIdCard
                ? required(request.idCard(), "身份证号").toUpperCase(Locale.ROOT)
                : trimToNull(request.idCard());
        if (idCard != null) {
            idCard = idCard.toUpperCase(Locale.ROOT);
        }
        String phone = required(request.phone(), "手机号");
        String email = trimToNull(request.email());
        if (!STUDENT_NO.matcher(studentNo).matches()) {
            throw new BusinessException(40001, "学号格式不正确");
        }
        if (name.length() > 50) {
            throw new BusinessException(40001, "姓名不能超过 50 个字符");
        }
        if (idCard != null && !validIdCard(idCard)) {
            throw new BusinessException(40001, "身份证号校验失败");
        }
        if (!PHONE.matcher(phone).matches()) {
            throw new BusinessException(40001, "手机号格式不正确");
        }
        if (email != null && !EMAIL.matcher(email).matches()) {
            throw new BusinessException(40001, "邮箱格式不正确");
        }
        if (request.gender() != null && request.gender() != 1 && request.gender() != 2) {
            throw new BusinessException(40001, "性别格式不正确");
        }
        int currentYear = Year.now().getValue();
        if (request.enrollYear() == null || request.enrollYear() < 2000 || request.enrollYear() > currentYear + 1) {
            throw new BusinessException(40001, "入学年份不正确");
        }
        if (request.collegeId() == null || request.collegeId() <= 0
                || request.majorId() == null || request.majorId() <= 0) {
            throw new BusinessException(40001, "学院和专业不能为空");
        }
        return new StudentRequest(
                studentNo, name, request.gender(), idCard, phone, email, request.enrollYear(),
                request.collegeId(), request.majorId(), request.classId(),
                StringUtils.hasText(request.studentType()) ? request.studentType().trim() : "本科");
    }

    private boolean validIdCard(String value) {
        if (!value.matches("\\d{17}[0-9X]")) {
            return false;
        }
        int sum = 0;
        for (int index = 0; index < 17; index++) {
            sum += (value.charAt(index) - '0') * ID_WEIGHTS[index];
        }
        return ID_CHECK[sum % 11] == value.charAt(17);
    }

    private String required(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(40001, field + "不能为空");
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String safeStudentNo(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }
}

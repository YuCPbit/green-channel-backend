package edu.greenchannel.student;

import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.common.PageResult;
import edu.greenchannel.operationlog.OperationLog;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/school/students")
public class StudentController {
    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    @RequirePermission("school:student:view")
    public ApiResponse<PageResult<StudentView>> search(
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) Integer enrollYear,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.search(studentNo, name, collegeId, enrollYear, page, size));
    }

    @GetMapping("/import-template")
    @RequirePermission("school:student:view")
    public ResponseEntity<byte[]> template() {
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("new-student-import-template.xlsx", StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(XLSX)
                .body(service.importTemplate());
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequirePermission("school:student:edit")
    @OperationLog(module = "新生信息", action = "IMPORT")
    public ApiResponse<StudentImportResult> importExcel(@RequestPart("file") MultipartFile file) {
        return ApiResponse.success(service.importExcel(file));
    }

    @GetMapping("/imports/{reportId}/errors")
    @RequirePermission("school:student:view")
    public ResponseEntity<byte[]> importErrors(@PathVariable String reportId) {
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("student-import-errors.xlsx", StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(XLSX)
                .body(service.importErrorReport(reportId));
    }

    @PostMapping
    @RequirePermission("school:student:edit")
    @OperationLog(module = "新生信息", action = "CREATE", targetId = "#result.data.id")
    public ApiResponse<StudentView> create(@RequestBody StudentRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @PutMapping("/{id}")
    @RequirePermission("school:student:edit")
    @OperationLog(module = "新生信息", action = "UPDATE", targetId = "#id")
    public ApiResponse<StudentView> update(@PathVariable long id, @RequestBody StudentRequest request) {
        return ApiResponse.success(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("school:student:edit")
    @OperationLog(module = "新生信息", action = "DELETE", targetId = "#id")
    public ApiResponse<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/batch")
    @RequirePermission("school:student:edit")
    @OperationLog(module = "新生信息", action = "BATCH_DELETE")
    public ApiResponse<Integer> batchDelete(@RequestBody StudentBatchDeleteRequest request) {
        return ApiResponse.success(service.batchDelete(request));
    }
}

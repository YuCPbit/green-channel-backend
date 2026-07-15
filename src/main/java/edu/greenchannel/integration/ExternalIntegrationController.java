package edu.greenchannel.integration;

import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.student.StudentRequest;
import edu.greenchannel.student.StudentService;
import edu.greenchannel.student.StudentView;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/external")
public class ExternalIntegrationController {
    private final StudentService studentService;

    public ExternalIntegrationController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/enrollment/students")
    public ApiResponse<StudentView> syncEnrollmentStudent(@RequestBody StudentRequest request) {
        return ApiResponse.success(studentService.create(request));
    }

    @GetMapping("/academic/students/{studentNo}")
    public ApiResponse<AcademicStudentStatus> academicStatus(@PathVariable String studentNo) {
        if (!StringUtils.hasText(studentNo) || !studentNo.matches("[A-Za-z0-9_-]{4,30}")) {
            throw new BusinessException(40001, "学号格式不正确");
        }
        return ApiResponse.success(new AcademicStudentStatus(
                studentNo.toUpperCase(), "ACTIVE", true, "MOCK_ACADEMIC"));
    }

    @PostMapping("/green-channel/status")
    public ApiResponse<String> receiveGreenChannelStatus(@RequestBody GreenChannelStatusRequest request) {
        if (request == null || !StringUtils.hasText(request.studentNo())
                || !StringUtils.hasText(request.businessId()) || !StringUtils.hasText(request.status())) {
            throw new BusinessException(40001, "状态回传字段不完整");
        }
        return ApiResponse.success("ACCEPTED");
    }

    @PostMapping("/sso/verify")
    public ApiResponse<SsoIdentity> verifySso(@RequestBody SsoVerifyRequest request) {
        String ticket = request == null ? null : request.ticket();
        if (!StringUtils.hasText(ticket) || !ticket.matches("MOCK-[A-Za-z0-9_-]{4,30}")) {
            return ApiResponse.success(new SsoIdentity(false, null, "MOCK_SSO"));
        }
        return ApiResponse.success(new SsoIdentity(true, ticket.substring(5), "MOCK_SSO"));
    }
}

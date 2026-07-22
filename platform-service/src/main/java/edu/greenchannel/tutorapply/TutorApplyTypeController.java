package edu.greenchannel.tutorapply;

import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.operationlog.OperationLog;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TutorApplyTypeController {
    private final TutorApplyTypeService service;

    public TutorApplyTypeController(TutorApplyTypeService service) {
        this.service = service;
    }

    @GetMapping("/api/tutor-apply-types")
    @RequirePermission("tutor:application:view")
    public ApiResponse<List<TutorApplyType>> enabledTypes() {
        return ApiResponse.success(service.enabledTypes());
    }

    @GetMapping("/api/school/tutor-apply-types")
    @RequirePermission("school:tutor-type:edit")
    public ApiResponse<List<TutorApplyType>> allTypes() {
        return ApiResponse.success(service.allTypes());
    }

    @PostMapping("/api/school/tutor-apply-types")
    @RequirePermission("school:tutor-type:edit")
    @OperationLog(module = "辅导员事务类型", action = "CREATE", targetId = "#result.data.id")
    public ApiResponse<TutorApplyType> create(@RequestBody TutorApplyTypeRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @PutMapping("/api/school/tutor-apply-types/{id}")
    @RequirePermission("school:tutor-type:edit")
    @OperationLog(module = "辅导员事务类型", action = "UPDATE", targetId = "#id")
    public ApiResponse<TutorApplyType> update(
            @PathVariable long id, @RequestBody TutorApplyTypeRequest request) {
        return ApiResponse.success(service.update(id, request));
    }

    @DeleteMapping("/api/school/tutor-apply-types/{id}")
    @RequirePermission("school:tutor-type:edit")
    @OperationLog(module = "辅导员事务类型", action = "DELETE", targetId = "#id")
    public ApiResponse<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}

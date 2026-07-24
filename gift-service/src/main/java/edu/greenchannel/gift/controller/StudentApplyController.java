package edu.greenchannel.gift.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.gift.dto.review.StudentApplyUpdateDTO;
import edu.greenchannel.gift.entity.StudentApply;
import edu.greenchannel.gift.service.StudentApplyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gift/apply")
public class StudentApplyController {

    private final StudentApplyService applyService;

    public StudentApplyController(StudentApplyService applyService) {
        this.applyService = applyService;
    }

    // 提交申请
    @PostMapping("/add")
    public ApiResponse<String> add(@RequestBody StudentApply apply) {
        apply.setStatus(2);
        applyService.save(apply);
        return ApiResponse.success("申请提交成功");
    }

    // 根据ID查询单条申请
    @GetMapping("/{id}")
    public ApiResponse<StudentApply> getById(@PathVariable Long id) {
        StudentApply apply = applyService.getById(id);
        return ApiResponse.success(apply);
    }

    // 新增
    @GetMapping("/list")
    public ApiResponse<List<StudentApply>> list(
            @RequestParam(required = false) Long packBatchId,
            @RequestParam(required = false) Long studentId
    ) {
        LambdaQueryWrapper<StudentApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentApply::getIsDeleted, 0);
        if (packBatchId != null) {
            wrapper.eq(StudentApply::getPackBatchId, packBatchId);
        }
        if (studentId != null) {
            wrapper.eq(StudentApply::getStudentId, studentId);
        }
        wrapper.orderByDesc(StudentApply::getCreateTime);
        List<StudentApply> list = applyService.list(wrapper);
        return ApiResponse.success(list);
    }


    // 申请分页列表
    @GetMapping("/page")
    public ApiResponse<IPage<StudentApply>> page(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize
    ) {
        Page<StudentApply> page = new Page<>(pageNum, pageSize);
        IPage<StudentApply> pageData = applyService.page(page);
        return ApiResponse.success(pageData);
    }

    // 修改申请信息
    @PutMapping("/edit")
    public ApiResponse<String> edit(@RequestBody StudentApply apply) {
        applyService.updateById(apply);
        return ApiResponse.success("申请修改成功");
    }

    /**
     * 驳回后修改申请并重新提交至辅导员审核
     */
    @PostMapping("/resubmit")
    public ApiResponse<String> reSubmit(@RequestBody @Valid StudentApplyUpdateDTO dto) {
        applyService.reSubmitAfterReject(dto);
        return ApiResponse.success("重新提交成功，待辅导员审核");
    }

    // 礼包领取状态更新
    @PutMapping("/pickup")
    public ApiResponse<String> pickup(@RequestBody StudentApply apply) {
        applyService.updateById(apply);
        return ApiResponse.success("领取状态更新成功");
    }

    // 删除申请记录
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        applyService.removeById(id);
        return ApiResponse.success("申请删除成功");
    }
}
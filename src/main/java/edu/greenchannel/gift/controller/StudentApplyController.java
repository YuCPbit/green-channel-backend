package edu.greenchannel.gift.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.gift.entity.StudentApply;
import edu.greenchannel.gift.service.StudentApplyService;
import org.springframework.web.bind.annotation.*;

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
        applyService.save(apply);
        return ApiResponse.success("申请提交成功");
    }

    // 根据ID查询单条申请
    @GetMapping("/{id}")
    public ApiResponse<StudentApply> getById(@PathVariable Long id) {
        StudentApply apply = applyService.getById(id);
        return ApiResponse.success(apply);
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
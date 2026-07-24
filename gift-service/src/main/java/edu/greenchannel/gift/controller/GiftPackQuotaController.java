package edu.greenchannel.gift.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.gift.entity.GiftPackQuota;
import edu.greenchannel.gift.service.GiftPackQuotaService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/gift/quota")
@RequirePermission("gift:quota:manage")
public class GiftPackQuotaController {

    private final GiftPackQuotaService quotaService;

    public GiftPackQuotaController(GiftPackQuotaService quotaService) {
        this.quotaService = quotaService;
    }

    // 新增名额分配
    @PostMapping("/add")
    public ApiResponse<String> add(@RequestBody GiftPackQuota quota) {
        quotaService.save(quota);
        return ApiResponse.success("名额分配创建成功");
    }

    // 根据ID单条查询
    @GetMapping("/{id}")
    public ApiResponse<GiftPackQuota> getById(@PathVariable Long id) {
        GiftPackQuota quota = quotaService.getById(id);
        return ApiResponse.success(quota);
    }

    // 分页列表
    @GetMapping("/page")
    public ApiResponse<IPage<GiftPackQuota>> page(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize
    ) {
        Page<GiftPackQuota> page = new Page<>(pageNum, pageSize);
        IPage<GiftPackQuota> pageData = quotaService.page(page);
        return ApiResponse.success(pageData);
    }

    // 新增
    @GetMapping("/list")
    public ApiResponse<List<GiftPackQuota>> list() {
        List<GiftPackQuota> list = quotaService.list();
        return ApiResponse.success(list);
    }

    // 修改名额
    @PutMapping("/edit")
    public ApiResponse<String> edit(@RequestBody GiftPackQuota quota) {
        if (quota.getId() == null) {
            // 替代 ApiResponse.fail("xxx") 的临时写法
            return new ApiResponse<>(-1, "申请ID不能为空", null, Instant.now());
        }
        quotaService.updateById(quota);
        return ApiResponse.success("名额信息更新成功");
    }

    // 逻辑删除
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        quotaService.removeById(id);
        return ApiResponse.success("名额记录删除成功");
    }
}

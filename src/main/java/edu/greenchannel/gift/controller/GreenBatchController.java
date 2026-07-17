package edu.greenchannel.gift.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.gift.entity.GreenChannelBatch;
import edu.greenchannel.gift.service.GreenChannelBatchService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gift/batch")
public class GreenBatchController {

    private final GreenChannelBatchService batchService;

    // 构造注入
    public GreenBatchController(GreenChannelBatchService batchService) {
        this.batchService = batchService;
    }

    // 新增批次
    @PostMapping("/add")
    public ApiResponse<String> add(@RequestBody GreenChannelBatch batch) {
        batchService.save(batch);
        return ApiResponse.success("批次创建成功");
    }

    // 根据ID查询单条批次
    @GetMapping("/{id}")
    public ApiResponse<GreenChannelBatch> getById(@PathVariable Long id) {
        GreenChannelBatch batch = batchService.getById(id);
        return ApiResponse.success(batch);
    }

    // 批次分页查询
    @GetMapping("/page")
    public ApiResponse<IPage<GreenChannelBatch>> page(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize
    ) {
        Page<GreenChannelBatch> page = new Page<>(pageNum, pageSize);
        IPage<GreenChannelBatch> pageData = batchService.page(page);
        return ApiResponse.success(pageData);
    }

    // 修改批次
    @PutMapping("/edit")
    public ApiResponse<String> edit(@RequestBody GreenChannelBatch batch) {
        batchService.updateById(batch);
        return ApiResponse.success("批次修改成功");
    }

    // 逻辑删除批次
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        batchService.removeById(id);
        return ApiResponse.success("删除成功");
    }
}
package edu.greenchannel.gift.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.gift.entity.GiftPackBatch;
import edu.greenchannel.gift.service.GiftPackBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gift/pack-batch")
@RequiredArgsConstructor
@RequirePermission("gift:pack-batch:manage")
public class GiftPackBatchController {

    private final GiftPackBatchService packBatchService;

    // 新增礼包批次
    @PostMapping("/add")
    public ApiResponse<String> add(@RequestBody GiftPackBatch packBatch) {
        packBatchService.save(packBatch);
        return ApiResponse.success("礼包批次创建成功");
    }

    // 查询礼包批次列表
    @GetMapping("/list")
    @RequirePermission({"student:green:view", "gift:pack-batch:manage"})
    public ApiResponse<List<GiftPackBatch>> list() {
        List<GiftPackBatch> list = packBatchService.list();
        return ApiResponse.success(list);
    }

    // 根据绿色通道批次ID查询礼包批次
    @GetMapping("/by-green-batch/{gcBatchId}")
    public ApiResponse<GiftPackBatch> getByGreenBatch(@PathVariable Long gcBatchId) {
        LambdaQueryWrapper<GiftPackBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GiftPackBatch::getGcBatchId, gcBatchId);
        GiftPackBatch packBatch = packBatchService.getOne(wrapper);
        return ApiResponse.success(packBatch);
    }

    // 修改礼包批次
    @PutMapping("/edit")
    public ApiResponse<String> edit(@RequestBody GiftPackBatch packBatch) {
        packBatchService.updateById(packBatch);
        return ApiResponse.success("礼包批次修改成功");
    }

    // 删除礼包批次
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        packBatchService.removeById(id);
        return ApiResponse.success("删除成功");
    }
}

package edu.greenchannel.gift.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.gift.entity.GiftPackItem;
import edu.greenchannel.gift.service.GiftPackItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gift/item")
public class GiftPackItemController {

    private final GiftPackItemService itemService;

    public GiftPackItemController(GiftPackItemService itemService) {
        this.itemService = itemService;
    }

    // 新增礼品
    @PostMapping("/add")
    public ApiResponse<String> add(@RequestBody GiftPackItem item) {
        itemService.save(item);
        return ApiResponse.success("礼品新增成功");
    }

    // 根据ID单条查询
    @GetMapping("/{id}")
    public ApiResponse<GiftPackItem> getById(@PathVariable Long id) {
        GiftPackItem item = itemService.getById(id);
        return ApiResponse.success(item);
    }

    // 分页列表查询（保留，以后用）
    @GetMapping("/page")
    public ApiResponse<IPage<GiftPackItem>> page(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize
    ) {
        Page<GiftPackItem> page = new Page<>(pageNum, pageSize);
        IPage<GiftPackItem> pageData = itemService.page(page);
        return ApiResponse.success(pageData);
    }

    // ✅ 新增：不分页，返回全部（给前端演示用）
    @GetMapping("/list")
    public ApiResponse<List<GiftPackItem>> list() {
        List<GiftPackItem> list = itemService.list();
        return ApiResponse.success(list);
    }

    // 修改礼品
    @PutMapping("/edit")
    public ApiResponse<String> edit(@RequestBody GiftPackItem item) {
        itemService.updateById(item);
        return ApiResponse.success("礼品修改成功");
    }

    // 删除礼品
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        itemService.removeById(id);
        return ApiResponse.success("礼品删除成功");
    }
}
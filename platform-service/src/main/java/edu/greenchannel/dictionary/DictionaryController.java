package edu.greenchannel.dictionary;

import edu.greenchannel.auth.RequirePermission;
import edu.greenchannel.common.ApiResponse;
import edu.greenchannel.common.PageResult;
import edu.greenchannel.operationlog.OperationLog;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DictionaryController {
    private final DictionaryService service;

    public DictionaryController(DictionaryService service) {
        this.service = service;
    }

    @GetMapping("/api/dictionaries/{dictTypeCode}")
    public ApiResponse<List<DictionaryItem>> enabledItems(@PathVariable String dictTypeCode) {
        return ApiResponse.success(service.enabledItems(dictTypeCode));
    }

    @GetMapping("/api/system/dictionaries")
    @RequirePermission("system:dictionary:view")
    public ApiResponse<PageResult<DictionaryItem>> search(
            @RequestParam(required = false) String dictTypeCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.search(dictTypeCode, page, size));
    }

    @PostMapping("/api/system/dictionaries/items")
    @RequirePermission("system:dictionary:edit")
    @OperationLog(module = "字典参数", action = "CREATE", targetId = "#result.data.id")
    public ApiResponse<DictionaryItem> create(@RequestBody DictionaryItemRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @PutMapping("/api/system/dictionaries/items/{id}")
    @RequirePermission("system:dictionary:edit")
    @OperationLog(module = "字典参数", action = "UPDATE", targetId = "#id")
    public ApiResponse<DictionaryItem> update(@PathVariable long id, @RequestBody DictionaryItemRequest request) {
        return ApiResponse.success(service.update(id, request));
    }

    @DeleteMapping("/api/system/dictionaries/items/{id}")
    @RequirePermission("system:dictionary:edit")
    @OperationLog(module = "字典参数", action = "DELETE", targetId = "#id")
    public ApiResponse<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}

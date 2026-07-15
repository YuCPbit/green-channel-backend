package edu.greenchannel.dictionary;

import edu.greenchannel.common.BusinessException;
import edu.greenchannel.common.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DictionaryService {
    private static final int MAX_PAGE_SIZE = 100;
    private final DictionaryRepository repository;
    private final Clock clock;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public DictionaryService(DictionaryRepository repository) {
        this(repository, Clock.systemUTC());
    }

    DictionaryService(DictionaryRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public List<DictionaryItem> enabledItems(String dictTypeCode) {
        String normalized = normalizeCode(dictTypeCode, "dictTypeCode");
        Instant now = clock.instant();
        CacheEntry cached = cache.get(normalized);
        if (cached != null && cached.expiresAt().isAfter(now)) {
            return cached.items();
        }
        List<DictionaryItem> items = List.copyOf(repository.findEnabledByType(normalized));
        cache.put(normalized, new CacheEntry(items, now.plus(5, ChronoUnit.MINUTES)));
        return items;
    }

    public PageResult<DictionaryItem> search(String dictTypeCode, int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(40001, "分页参数不正确");
        }
        String normalized = StringUtils.hasText(dictTypeCode) ? normalizeCode(dictTypeCode, "dictTypeCode") : null;
        return repository.search(normalized, page, size);
    }

    public DictionaryItem create(DictionaryItemRequest request) {
        DictionaryItem item = fromRequest(0, request);
        DictionaryItem created = repository.insert(item);
        invalidate(created.dictTypeCode());
        return created;
    }

    public DictionaryItem update(long id, DictionaryItemRequest request) {
        DictionaryItem existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40400, "字典项不存在"));
        DictionaryItem updated = fromRequest(id, request);
        repository.update(updated);
        invalidate(existing.dictTypeCode());
        invalidate(updated.dictTypeCode());
        return updated;
    }

    public void delete(long id) {
        DictionaryItem existing = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40400, "字典项不存在"));
        repository.softDelete(id);
        invalidate(existing.dictTypeCode());
    }

    private DictionaryItem fromRequest(long id, DictionaryItemRequest request) {
        if (request == null || !StringUtils.hasText(request.dictTypeName()) ||
                !StringUtils.hasText(request.itemName())) {
            throw new BusinessException(40001, "字典类型名称和字典项名称不能为空");
        }
        String typeCode = normalizeCode(request.dictTypeCode(), "dictTypeCode");
        String itemCode = normalizeCode(request.itemCode(), "itemCode");
        int sort = request.sort() == null ? 0 : request.sort();
        boolean enabled = request.enabled() == null || request.enabled();
        return new DictionaryItem(id, typeCode, request.dictTypeName().trim(), itemCode,
                request.itemName().trim(), trimToNull(request.itemValue()), sort, enabled, trimToNull(request.remark()));
    }

    private String normalizeCode(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(40001, field + " 不能为空");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9][A-Z0-9_-]{0,49}")) {
            throw new BusinessException(40001, field + " 格式不正确");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private void invalidate(String typeCode) {
        cache.remove(typeCode);
    }

    private record CacheEntry(List<DictionaryItem> items, Instant expiresAt) {
    }
}

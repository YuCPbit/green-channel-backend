package edu.greenchannel.dictionary;

import edu.greenchannel.common.PageResult;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DictionaryServiceTest {
    @Test
    void cachesEnabledItemsAndInvalidatesAfterUpdate() {
        InMemoryRepository repository = new InMemoryRepository();
        DictionaryService service = new DictionaryService(
                repository, Clock.fixed(Instant.parse("2026-07-15T00:00:00Z"), ZoneOffset.UTC));

        service.enabledItems("apply_status");
        service.enabledItems("APPLY_STATUS");
        assertEquals(1, repository.readCount);

        service.update(1, new DictionaryItemRequest(
                "APPLY_STATUS", "申请状态", "1", "草稿（已更新）", null, 1, true, null));
        service.enabledItems("APPLY_STATUS");
        assertEquals(2, repository.readCount);
    }

    @Test
    void deleteUsesLogicalDeleteAndClearsCache() {
        InMemoryRepository repository = new InMemoryRepository();
        DictionaryService service = new DictionaryService(repository);

        service.enabledItems("APPLY_STATUS");
        service.delete(1);

        assertTrue(repository.deleted);
    }

    private static class InMemoryRepository implements DictionaryRepository {
        private DictionaryItem item = new DictionaryItem(
                1, "APPLY_STATUS", "申请状态", "1", "草稿", null, 1, true, null);
        private int readCount;
        private boolean deleted;

        @Override
        public List<DictionaryItem> findEnabledByType(String dictTypeCode) {
            readCount++;
            return List.of(item);
        }

        @Override
        public PageResult<DictionaryItem> search(String dictTypeCode, int page, int size) {
            return new PageResult<>(List.of(item), 1, page, size);
        }

        @Override
        public Optional<DictionaryItem> findById(long id) {
            return Optional.ofNullable(deleted ? null : item);
        }

        @Override
        public DictionaryItem insert(DictionaryItem value) {
            item = new DictionaryItem(1, value.dictTypeCode(), value.dictTypeName(), value.itemCode(),
                    value.itemName(), value.itemValue(), value.sort(), value.enabled(), value.remark());
            return item;
        }

        @Override
        public void update(DictionaryItem value) {
            item = value;
        }

        @Override
        public void softDelete(long id) {
            deleted = true;
        }
    }
}

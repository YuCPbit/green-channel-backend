package edu.greenchannel.tutorapply;

import edu.greenchannel.common.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TutorApplyTypeServiceTest {
    private final MemoryRepository repository = new MemoryRepository();
    private final TutorApplyTypeService service = new TutorApplyTypeService(repository);

    @Test
    void createsDynamicTypeWithNormalizedCode() {
        TutorApplyType created = service.create(new TutorApplyTypeRequest(
                "测试申请", "test_type", null, true, false, 2,
                List.of(java.util.Map.of("key", "reason", "type", "text")), 1, true));

        assertEquals("TEST_TYPE", created.typeCode());
        assertEquals(1, service.enabledTypes().size());
    }

    @Test
    void rejectsInvalidApprovalLevelAndDuplicateCode() {
        service.create(new TutorApplyTypeRequest(
                "测试申请", "TEST_TYPE", null, false, true, 1, List.of(), 1, true));

        assertThrows(BusinessException.class, () -> service.create(new TutorApplyTypeRequest(
                "重复申请", "TEST_TYPE", null, false, true, 2, List.of(), 2, true)));
        assertThrows(BusinessException.class, () -> service.create(new TutorApplyTypeRequest(
                "错误审批", "INVALID_LEVEL", null, false, true, 3, List.of(), 3, true)));
    }

    private static class MemoryRepository implements TutorApplyTypeRepository {
        private final List<TutorApplyType> values = new ArrayList<>();

        @Override
        public List<TutorApplyType> findAll(boolean enabledOnly) {
            return values.stream().filter(value -> !enabledOnly || value.enabled()).toList();
        }

        @Override
        public Optional<TutorApplyType> findById(long id) {
            return values.stream().filter(value -> value.id() == id).findFirst();
        }

        @Override
        public boolean existsByCode(String code, Long excludedId) {
            return values.stream().anyMatch(value -> value.typeCode().equals(code)
                    && (excludedId == null || value.id() != excludedId));
        }

        @Override
        public TutorApplyType insert(TutorApplyType type) {
            TutorApplyType saved = new TutorApplyType(
                    values.size() + 1L, type.typeName(), type.typeCode(), type.description(),
                    type.needAmount(), type.needStudent(), type.approvalLevel(), type.formTemplate(),
                    type.sort(), type.enabled());
            values.add(saved);
            return saved;
        }

        @Override
        public TutorApplyType update(TutorApplyType type) {
            values.replaceAll(value -> value.id() == type.id() ? type : value);
            return type;
        }

        @Override
        public void softDelete(long id) {
            values.removeIf(value -> value.id() == id);
        }
    }
}

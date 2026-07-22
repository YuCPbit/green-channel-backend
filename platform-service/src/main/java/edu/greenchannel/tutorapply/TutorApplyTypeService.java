package edu.greenchannel.tutorapply;

import edu.greenchannel.common.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

@Service
public class TutorApplyTypeService {
    private final TutorApplyTypeRepository repository;

    public TutorApplyTypeService(TutorApplyTypeRepository repository) {
        this.repository = repository;
    }

    public List<TutorApplyType> enabledTypes() {
        return repository.findAll(true);
    }

    public List<TutorApplyType> allTypes() {
        return repository.findAll(false);
    }

    @Transactional
    public TutorApplyType create(TutorApplyTypeRequest request) {
        TutorApplyType value = normalize(0, request);
        ensureUnique(value.typeCode(), null);
        return repository.insert(value);
    }

    @Transactional
    public TutorApplyType update(long id, TutorApplyTypeRequest request) {
        repository.findById(id).orElseThrow(() -> new BusinessException(40400, "申请类型不存在"));
        TutorApplyType value = normalize(id, request);
        ensureUnique(value.typeCode(), id);
        return repository.update(value);
    }

    @Transactional
    public void delete(long id) {
        repository.findById(id).orElseThrow(() -> new BusinessException(40400, "申请类型不存在"));
        repository.softDelete(id);
    }

    private TutorApplyType normalize(long id, TutorApplyTypeRequest request) {
        if (request == null || !StringUtils.hasText(request.typeName())
                || !StringUtils.hasText(request.typeCode())) {
            throw new BusinessException(40001, "类型名称和编码不能为空");
        }
        String code = request.typeCode().trim().toUpperCase(Locale.ROOT);
        if (!code.matches("[A-Z][A-Z0-9_]{2,49}")) {
            throw new BusinessException(40001, "类型编码格式不正确");
        }
        int approvalLevel = request.approvalLevel() == null ? 2 : request.approvalLevel();
        if (approvalLevel != 1 && approvalLevel != 2) {
            throw new BusinessException(40001, "审批级数只能是 1 或 2");
        }
        if (request.formTemplate() != null && !(request.formTemplate() instanceof List<?>)) {
            throw new BusinessException(40001, "表单模板必须是字段数组");
        }
        return new TutorApplyType(
                id, request.typeName().trim(), code, trimToNull(request.description()),
                Boolean.TRUE.equals(request.needAmount()), request.needStudent() == null || request.needStudent(),
                approvalLevel, request.formTemplate(), request.sort() == null ? 0 : request.sort(),
                request.enabled() == null || request.enabled());
    }

    private void ensureUnique(String code, Long excludedId) {
        if (repository.existsByCode(code, excludedId)) {
            throw new BusinessException(40900, "申请类型编码已存在");
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}

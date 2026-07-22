package edu.greenchannel.dictionary;

import edu.greenchannel.common.PageResult;

import java.util.List;
import java.util.Optional;

public interface DictionaryRepository {
    List<DictionaryItem> findEnabledByType(String dictTypeCode);

    PageResult<DictionaryItem> search(String dictTypeCode, int page, int size);

    Optional<DictionaryItem> findById(long id);

    DictionaryItem insert(DictionaryItem item);

    void update(DictionaryItem item);

    void softDelete(long id);
}

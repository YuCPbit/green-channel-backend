package edu.greenchannel.dictionary;

public record DictionaryItemRequest(
        String dictTypeCode,
        String dictTypeName,
        String itemCode,
        String itemName,
        String itemValue,
        Integer sort,
        Boolean enabled,
        String remark
) {
}

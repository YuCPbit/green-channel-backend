package edu.greenchannel.dictionary;

public record DictionaryItem(
        long id,
        String dictTypeCode,
        String dictTypeName,
        String itemCode,
        String itemName,
        String itemValue,
        int sort,
        boolean enabled,
        String remark
) {
}

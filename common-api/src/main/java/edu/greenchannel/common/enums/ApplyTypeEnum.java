package edu.greenchannel.common.enums;

/**
 * 统一审核流水中的业务申请类型。
 */
public enum ApplyTypeEnum {
    GIFT_APPLY(1),
    SUBSIDY_APPLY(2),
    APPEAL(3),
    TUTOR_AFFAIR(4);

    private final Integer code;

    ApplyTypeEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}

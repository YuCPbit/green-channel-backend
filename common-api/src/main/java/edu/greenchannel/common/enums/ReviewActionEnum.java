package edu.greenchannel.common.enums;

/**
 * 各业务审批流程共用的审核动作。
 */
public enum ReviewActionEnum {
    PASS(1),
    REJECT_MODIFY(2),
    REJECT_NO_PASS(3);

    private final Integer code;

    ReviewActionEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}

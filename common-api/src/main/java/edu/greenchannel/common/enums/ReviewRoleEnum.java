package edu.greenchannel.common.enums;

/**
 * 审核角色编码与 gc_user.user_type 保持一致。
 */
public enum ReviewRoleEnum {
    TUTOR(2),
    COLLEGE_ADMIN(3),
    SCHOOL_ADMIN(4);

    private final Integer code;

    ReviewRoleEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}

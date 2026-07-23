package edu.greenchannel.tutor.enums;

/**
 * 辅导员事务申请状态枚举
 */
public enum TutorAppStatus {
    DRAFT(1, "草稿"),
    PENDING_COLLEGE(2, "待学院审批"),
    PENDING_SCHOOL(3, "待学校审批"),
    APPROVED(4, "已通过"),
    REJECTED(5, "已驳回");

    private final int code;
    private final String desc;

    TutorAppStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static TutorAppStatus fromCode(int code) {
        for (TutorAppStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("未知申请状态: " + code);
    }
}

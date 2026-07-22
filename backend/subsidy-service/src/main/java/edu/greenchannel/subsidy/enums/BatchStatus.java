package edu.greenchannel.subsidy.enums;

public enum BatchStatus {
    DRAFT(0, "草稿"),
    ACTIVE(1, "进行中"),
    ENDED(2, "已结束");

    private final int code;
    private final String desc;

    BatchStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static BatchStatus fromCode(int code) {
        for (BatchStatus status : values()) {
            if (status.code == code) return status;
        }
        return DRAFT;
    }
}
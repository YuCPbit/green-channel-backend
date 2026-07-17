package edu.greenchannel.subsidy.enums;

public enum AllocatorRole {
    SCHOOL(1, "学校资助中心"),
    COLLEGE(2, "学院管理员");

    private final int code;
    private final String desc;

    AllocatorRole(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }
}
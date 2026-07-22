package edu.greenchannel.subsidy.enums;

public enum TargetType {
    COLLEGE(1, "下发给学院"),
    GRADE(2, "下发给年级");

    private final int code;
    private final String desc;

    TargetType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }
}
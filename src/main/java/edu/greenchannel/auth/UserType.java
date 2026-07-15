package edu.greenchannel.auth;

import java.util.Arrays;
import java.util.List;

public enum UserType {
    STUDENT(1, "学生", List.of("首页", "绿色通道", "困难补助", "消息中心")),
    TUTOR(2, "辅导员", List.of("首页", "学生管理", "资助审核", "事务申请", "消息中心")),
    COLLEGE_ADMIN(3, "学院管理员", List.of("首页", "学院审核", "额度管理", "学院报表", "消息中心")),
    SCHOOL_ADMIN(4, "学校资助中心", List.of("首页", "批次配置", "学校审核", "资金管理", "数据看板", "消息中心")),
    SYSTEM_ADMIN(5, "系统管理员", List.of("首页", "用户管理", "角色权限", "字典参数", "接口监控", "操作日志"));

    private final int code;
    private final String displayName;
    private final List<String> menus;

    UserType(int code, String displayName, List<String> menus) {
        this.code = code;
        this.displayName = displayName;
        this.menus = menus;
    }

    public int getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getMenus() {
        return menus;
    }

    public static UserType fromCode(int code) {
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知用户类型: " + code));
    }
}


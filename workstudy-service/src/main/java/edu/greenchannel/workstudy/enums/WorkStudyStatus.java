// WorkStudyStatus.java - 勤工助学状态枚举
package edu.greenchannel.workstudy.enums;

/**
 * 勤工助学状态枚举
 */
public enum WorkStudyStatus {
    // 批次状态: 0-未开始 1-报名中 2-面试中 3-进行中 4-已结束
    BATCH_NOT_STARTED(0, "未开始"),
    BATCH_REGISTERING(1, "报名中"),
    BATCH_INTERVIEWING(2, "面试中"),
    BATCH_IN_PROGRESS(3, "进行中"),
    BATCH_FINISHED(4, "已结束"),

    // 岗位状态: 0-草稿 1-待审核 2-已上架 3-已下架 4-审核不通过
    POSITION_DRAFT(0, "草稿"),
    POSITION_PENDING_APPROVAL(1, "待审核"),
    POSITION_ONLINE(2, "已上架"),
    POSITION_OFFLINE(3, "已下架"),
    POSITION_REJECTED(4, "审核不通过"),

    // 申请状态: 1-已报名 2-面试中 3-待录用审批 4-已录用 5-未录用
    APPLY_SUBMITTED(1, "已报名"),
    APPLY_INTERVIEWING(2, "面试中"),
    APPLY_PENDING_APPROVAL(3, "待录用审批"),
    APPLY_HIRED(4, "已录用"),
    APPLY_REJECTED(5, "未录用"),

    // 面试状态: 0-待面试 1-已面试 2-面试通过 3-面试不通过
    INTERVIEW_PENDING(0, "待面试"),
    INTERVIEW_COMPLETED(1, "已面试"),
    INTERVIEW_PASSED(2, "面试通过"),
    INTERVIEW_FAILED(3, "面试不通过"),

    // 录用状态: 1-在岗 2-已调岗 3-主动离岗 4-违规解聘
    HIRE_ON_JOB(1, "在岗"),
    HIRE_TRANSFERRED(2, "已调岗"),
    HIRE_RESIGNED(3, "主动离岗"),
    HIRE_DISMISSED(4, "违规解聘"),

    // 考勤状态: 1-正常 2-迟到 3-早退 4-请假 5-旷工 6-补打卡待审批 7-补打卡已通过
    ATTENDANCE_NORMAL(1, "正常"),
    ATTENDANCE_LATE(2, "迟到"),
    ATTENDANCE_EARLY_LEAVE(3, "早退"),
    ATTENDANCE_LEAVE(4, "请假"),
    ATTENDANCE_ABSENT(5, "旷工"),
    ATTENDANCE_RETRY_PENDING(6, "补打卡待审批"),
    ATTENDANCE_RETRY_APPROVED(7, "补打卡已通过"),

    // 薪酬状态: 1-待部门确认 2-待资助中心审批 3-已审批 4-已发放
    SALARY_DEPT_PENDING(1, "待部门确认"),
    SALARY_SCHOOL_PENDING(2, "待资助中心审批"),
    SALARY_APPROVED(3, "已审批"),
    SALARY_PAID(4, "已发放"),

    // 协议签署状态: 0-待签署 1-已签署 2-已到期 3-已续签
    AGREEMENT_PENDING(0, "待签署"),
    AGREEMENT_SIGNED(1, "已签署"),
    AGREEMENT_EXPIRED(2, "已到期"),
    AGREEMENT_RENEWED(3, "已续签");

    private final int code;
    private final String desc;

    WorkStudyStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static WorkStudyStatus fromCode(int code) {
        for (WorkStudyStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的状态码: " + code);
    }
}
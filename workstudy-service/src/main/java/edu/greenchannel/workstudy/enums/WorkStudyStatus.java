package edu.greenchannel.workstudy.enums;

public enum WorkStudyStatus {

    // 批次状态 (gc_work_study_batch.status)
    BATCH_NOT_STARTED(0, "未开始"),
    BATCH_REGISTERING(1, "报名中"),
    BATCH_INTERVIEWING(2, "面试中"),
    BATCH_IN_PROGRESS(3, "进行中"),
    BATCH_FINISHED(4, "已结束"),

    // 岗位状态 (gc_work_study_position.status)
    POSITION_DRAFT(0, "草稿"),
    POSITION_PENDING_APPROVAL(1, "待审核"),
    POSITION_ONLINE(2, "已上架"),
    POSITION_OFFLINE(3, "已下架"),
    POSITION_REJECTED(4, "审核不通过"),

    // 申请状态 (gc_work_study_apply.status)
    APPLY_SUBMITTED(1, "已报名"),
    APPLY_INTERVIEWING(2, "面试中"),
    APPLY_PENDING_APPROVAL(3, "待录用审批"),
    APPLY_HIRED(4, "已录用"),
    APPLY_REJECTED(5, "未录用"),

    // 面试状态 (gc_work_study_apply.interview_status)
    INTERVIEW_PENDING(0, "待面试"),
    INTERVIEW_COMPLETED(1, "已面试"), // 仅标记已面，未定结果
    INTERVIEW_PASSED(2, "面试通过"),
    INTERVIEW_FAILED(3, "面试不通过");

    private final int code;
    private final String desc;

    WorkStudyStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static WorkStudyStatus fromCode(int code) {
        for (WorkStudyStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("未知状态码: " + code);
    }
}
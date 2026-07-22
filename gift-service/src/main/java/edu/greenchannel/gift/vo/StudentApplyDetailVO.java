package edu.greenchannel.gift.vo;

import edu.greenchannel.gift.entity.StudentApply;
import edu.greenchannel.gift.entity.review.ReviewRecord;
import lombok.Data;
import java.util.List;

@Data
public class StudentApplyDetailVO {
    // 申请单基础信息
    private StudentApply studentApply;
    // 已完成的审批流水
    private List<ReviewRecord> reviewRecordList;
    // 前端展示拓展字段
    private String studentName;
    private String collegeName;
    private String className;
}
@Data
@TableName("gc_work_study_apply")
public class WorkStudyApply {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long positionId;
    private Long studentId;
    private String applyNo;
    private Integer status; // 1已报名 2已录用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
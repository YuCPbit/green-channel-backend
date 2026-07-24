package edu.greenchannel.gift.service.impl.review;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.auth.TokenService;
import edu.greenchannel.common.BusinessException;
import edu.greenchannel.common.enums.ApplyTypeEnum;
import edu.greenchannel.common.enums.ReviewActionEnum;
import edu.greenchannel.common.enums.ReviewRoleEnum;
import edu.greenchannel.gift.dto.review.BatchSubmitDTO;
import edu.greenchannel.gift.dto.review.GiftPickupDTO;
import edu.greenchannel.gift.dto.review.GiftReviewOperateDTO;
import edu.greenchannel.gift.dto.review.GiftSupplementDTO;
import edu.greenchannel.gift.entity.StudentApply;
import edu.greenchannel.gift.entity.review.ReviewRecord;
import edu.greenchannel.gift.mapper.ReviewRecordMapper;
import edu.greenchannel.gift.mapper.StudentApplyMapper;
import edu.greenchannel.gift.service.review.GiftReviewService;
import edu.greenchannel.gift.vo.StudentApplyDetailVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GiftReviewServiceImpl extends ServiceImpl<ReviewRecordMapper, ReviewRecord>
        implements GiftReviewService {

    private final StudentApplyMapper studentApplyMapper;
    private final HttpServletRequest request;
    private final TokenService tokenService;

    /**
     * 从请求头提取 Token
     */
    private String extractToken() {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


     //从 Token 获取当前登录用户

    private CurrentUser getCurrentUser() {
        String token = extractToken();
        if (token == null) {
            throw new BusinessException(40100, "请先登录");
        }
        return tokenService.resolve(token)
                .orElseThrow(() -> new BusinessException(40100, "Token 无效或已过期"));
    }
    /**
     * 从 Token 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        return getCurrentUser().id();
    }

    /**
     * 从 Token 获取当前登录用户角色
     */
    private Integer getCurrentRole() {
        return getCurrentUser().userType();
    }

    @Override
    public Page<StudentApplyDetailVO> listWaitReview(Long batchId, String studentName, Integer pageNum, Integer pageSize) {
        Page<StudentApply> pageParam = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StudentApply> queryWrapper = new LambdaQueryWrapper<>();

        if (batchId != null) {
            queryWrapper.eq(StudentApply::getPackBatchId, batchId);
        }
        queryWrapper.eq(StudentApply::getIsDeleted, 0);

        // 从 Token 获取真实角色
        Integer loginRole = getCurrentRole();

        // 根据角色过滤对应的审核状态
        if (ReviewRoleEnum.TUTOR.getCode().equals(loginRole)) {
            queryWrapper.eq(StudentApply::getStatus, 2);
        } else if (ReviewRoleEnum.COLLEGE_ADMIN.getCode().equals(loginRole)) {
            queryWrapper.eq(StudentApply::getStatus, 3);
        } else if (ReviewRoleEnum.SCHOOL_ADMIN.getCode().equals(loginRole)) {
            queryWrapper.eq(StudentApply::getStatus, 4);
        } else {
            throw new BusinessException(40300, "该角色无审核权限");
        }

        Page<StudentApply> rawPage = studentApplyMapper.selectPage(pageParam, queryWrapper);
        List<StudentApplyDetailVO> voList = rawPage.getRecords().stream()
                .map(item -> {
                    StudentApplyDetailVO vo = new StudentApplyDetailVO();
                    vo.setStudentApply(item);
                    vo.setReviewRecordList(this.getApplyReviewRecord(item.getId()));
                    return vo;
                }).toList();

        Page<StudentApplyDetailVO> resultPage = new Page<>(pageNum, pageSize);
        resultPage.setRecords(voList);
        resultPage.setTotal(rawPage.getTotal());
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewOperate(GiftReviewOperateDTO dto) {
        StudentApply apply = studentApplyMapper.selectById(dto.getApplyId());
        if (apply == null || Objects.equals(apply.getIsDeleted(), 1)) {
            throw new BusinessException(40400, "申请单据不存在或已删除");
        }

        // 从 Token 获取真实角色和ID
        Long reviewerId = getCurrentUserId();
        Integer currentRole = getCurrentRole();

        // 校验角色与状态匹配
        boolean match = checkRoleMatchStatus(currentRole, apply.getStatus());
        if (!match) {
            throw new BusinessException(40900, "当前单据不在你的审核节点，无法操作");
        }
        if (!Set.of(
                ReviewActionEnum.PASS.getCode(),
                ReviewActionEnum.REJECT_MODIFY.getCode(),
                ReviewActionEnum.REJECT_NO_PASS.getCode()).contains(dto.getAction())) {
            throw new BusinessException(40000, "不支持的审核操作");
        }

        // 插入审核流水
        ReviewRecord record = new ReviewRecord();
        record.setApplyId(dto.getApplyId());
        record.setApplyType(ApplyTypeEnum.GIFT_APPLY.getCode());
        record.setReviewerId(reviewerId);
        record.setReviewerRole(currentRole);
        record.setAction(dto.getAction());
        record.setComment(dto.getComment());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setIsDeleted(0);
        baseMapper.insert(record);

        if (updateApplyStatusByOperate(apply, dto.getAction(), currentRole) != 1) {
            throw new BusinessException(40900, "申请状态已发生变化，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSubmit(BatchSubmitDTO dto) {
        Integer currentRole = getCurrentRole();

        if (ReviewRoleEnum.SCHOOL_ADMIN.getCode().equals(currentRole)) {
            throw new BusinessException(40300, "学校账号无需批量提交操作");
        }
        if (!ReviewRoleEnum.TUTOR.getCode().equals(currentRole)
                && !ReviewRoleEnum.COLLEGE_ADMIN.getCode().equals(currentRole)) {
            throw new BusinessException(40300, "该角色无批量提交权限");
        }

        Integer currentStatus = currentRole == ReviewRoleEnum.TUTOR.getCode() ? 2 : 3;
        Integer targetStatus = currentRole == ReviewRoleEnum.TUTOR.getCode() ? 3 : 4;
        List<Long> ids = dto.getApplyIdList().stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty() || ids.size() != dto.getApplyIdList().size()) {
            throw new BusinessException(40000, "申请单列表包含空值或重复项");
        }

        LambdaUpdateWrapper<StudentApply> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(StudentApply::getId, ids)
                .eq(StudentApply::getStatus, currentStatus)
                .eq(StudentApply::getIsDeleted, 0)
                .set(StudentApply::getStatus, targetStatus)
                .set(StudentApply::getUpdateTime, LocalDateTime.now());
        int updated = studentApplyMapper.update(null, updateWrapper);
        if (updated != ids.size()) {
            throw new BusinessException(40900, "部分申请不在当前审核节点，请刷新后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPassApply(Long applyId) {
        Integer currentRole = getCurrentRole();
        if (!ReviewRoleEnum.SCHOOL_ADMIN.getCode().equals(currentRole)) {
            throw new BusinessException(40300, "仅学校管理员可取消已通过申请");
        }

        StudentApply apply = studentApplyMapper.selectById(applyId);
        if (apply == null || Objects.equals(apply.getIsDeleted(), 1)) {
            throw new BusinessException(40400, "申请单据不存在或已删除");
        }
        if (!Objects.equals(apply.getStatus(), 5)) {
            throw new BusinessException(40900, "仅已终审通过单据可取消");
        }

        LambdaUpdateWrapper<StudentApply> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(StudentApply::getId, applyId)
                .eq(StudentApply::getStatus, 5)
                .eq(StudentApply::getIsDeleted, 0)
                .set(StudentApply::getStatus, 2)
                .set(StudentApply::getUpdateTime, LocalDateTime.now());
        if (studentApplyMapper.update(null, updateWrapper) != 1) {
            throw new BusinessException(40900, "申请状态已发生变化，请刷新后重试");
        }
    }

    @Override
    public List<ReviewRecord> getApplyReviewRecord(Long applyId) {
        LambdaQueryWrapper<ReviewRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReviewRecord::getApplyId, applyId)
                .eq(ReviewRecord::getApplyType, ApplyTypeEnum.GIFT_APPLY.getCode())
                .eq(ReviewRecord::getIsDeleted, 0)
                .orderByAsc(ReviewRecord::getCreateTime);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pickup(GiftPickupDTO dto) {
        Long operatorId = getCurrentUserId();
        StudentApply apply = findPickupApplication(dto.getPickupCode());
        validateApprovedAndPending(apply);

        LambdaUpdateWrapper<StudentApply> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(StudentApply::getId, apply.getId())
                .eq(StudentApply::getPickupStatus, 0)
                .eq(StudentApply::getIsDeleted, 0)
                .set(StudentApply::getPickupStatus, 1)
                .set(StudentApply::getPickupTime, LocalDateTime.now())
                .set(StudentApply::getPickupOperatorId, operatorId)
                .set(StudentApply::getPickupRemark, normalizeRemark(dto.getRemark()))
                .set(StudentApply::getUpdateTime, LocalDateTime.now());
        ensureStateChanged(studentApplyMapper.update(null, updateWrapper), "该礼包已被其他工作人员核销，请勿重复操作");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pickupException(GiftPickupDTO dto) {
        Long operatorId = getCurrentUserId();
        StudentApply apply = findPickupApplication(dto.getPickupCode());
        validateApprovedAndPending(apply);
        String remark = normalizeRemark(dto.getRemark());
        if (remark == null) {
            throw new BusinessException(40000, "异常登记必须填写备注");
        }

        LambdaUpdateWrapper<StudentApply> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(StudentApply::getId, apply.getId())
                .eq(StudentApply::getPickupStatus, 0)
                .eq(StudentApply::getIsDeleted, 0)
                .set(StudentApply::getPickupStatus, 2)
                .set(StudentApply::getPickupRemark, remark)
                .set(StudentApply::getPickupOperatorId, operatorId)
                .set(StudentApply::getUpdateTime, LocalDateTime.now());
        ensureStateChanged(studentApplyMapper.update(null, updateWrapper), "该礼包状态已发生变化，请刷新后重试");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pickupReissue(GiftPickupDTO dto) {
        Long operatorId = getCurrentUserId();
        StudentApply apply = findPickupApplication(dto.getPickupCode());
        if (!Objects.equals(apply.getStatus(), 5)) {
            throw new BusinessException(40900, "申请未终审通过，无法补发");
        }
        if (!Objects.equals(apply.getPickupStatus(), 2)) {
            throw new BusinessException(40900, "仅异常待处理状态可执行补发操作");
        }

        LambdaUpdateWrapper<StudentApply> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(StudentApply::getId, apply.getId())
                .eq(StudentApply::getPickupStatus, 2)
                .eq(StudentApply::getIsDeleted, 0)
                .set(StudentApply::getPickupStatus, 3)
                .set(StudentApply::getPickupTime, LocalDateTime.now())
                .set(StudentApply::getPickupOperatorId, operatorId)
                .set(StudentApply::getPickupRemark, normalizeRemark(dto.getRemark()))
                .set(StudentApply::getUpdateTime, LocalDateTime.now());
        ensureStateChanged(studentApplyMapper.update(null, updateWrapper), "该礼包状态已发生变化，请刷新后重试");
    }

    private StudentApply findPickupApplication(String pickupCode) {
        String normalizedCode = pickupCode == null ? null : pickupCode.trim();
        if (normalizedCode == null || normalizedCode.isEmpty()) {
            throw new BusinessException(40000, "领取码不能为空");
        }
        LambdaQueryWrapper<StudentApply> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentApply::getPickupCode, normalizedCode)
                .eq(StudentApply::getIsDeleted, 0);
        StudentApply apply = studentApplyMapper.selectOne(queryWrapper);
        if (apply == null) {
            throw new BusinessException(40400, "领取码无效，未找到对应申请");
        }
        return apply;
    }

    private void validateApprovedAndPending(StudentApply apply) {
        if (!Objects.equals(apply.getStatus(), 5)) {
            throw new BusinessException(40900, "申请未终审通过，无法领取");
        }
        if (!Objects.equals(apply.getPickupStatus(), 0)) {
            throw new BusinessException(40900, "该礼包已领取或已登记异常，无法重复操作");
        }
    }

    private void ensureStateChanged(int updateCount, String message) {
        if (updateCount != 1) {
            throw new BusinessException(40900, message);
        }
    }

    private String normalizeRemark(String remark) {
        if (remark == null || remark.isBlank()) {
            return null;
        }
        return remark.trim();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void supplement(GiftSupplementDTO dto) {
        Long operatorId = getCurrentUserId();
        StudentApply apply = new StudentApply();
        apply.setStudentId(dto.getStudentId());
        apply.setPackBatchId(dto.getPackBatchId());
        apply.setApplyNo(dto.getApplyNo());
        apply.setApplyReason(dto.getApplyReason());
        apply.setPickupCode(dto.getPickupCode());
        apply.setStatus(5);
        apply.setPickupStatus(0);
        apply.setApplyTime(dto.getApplyTime() != null ? dto.getApplyTime() : LocalDateTime.now());
        apply.setIsDeleted(0);
        apply.setCreateTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());
        studentApplyMapper.insert(apply);

        ReviewRecord record = new ReviewRecord();
        record.setApplyId(apply.getId());
        record.setApplyType(ApplyTypeEnum.GIFT_APPLY.getCode());
        record.setReviewerId(operatorId);
        record.setReviewerRole(ReviewRoleEnum.SCHOOL_ADMIN.getCode());
        record.setAction(ReviewActionEnum.PASS.getCode());
        String comment = "历史数据补录";
        if (dto.getRemark() != null && !dto.getRemark().isBlank()) {
            comment += "：" + dto.getRemark();
        }
        record.setComment(comment);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setIsDeleted(0);
        baseMapper.insert(record);
    }

    private boolean checkRoleMatchStatus(Integer role, Integer status) {
        if (ReviewRoleEnum.TUTOR.getCode().equals(role) && status.equals(2)) return true;
        if (ReviewRoleEnum.COLLEGE_ADMIN.getCode().equals(role) && status.equals(3)) return true;
        if (ReviewRoleEnum.SCHOOL_ADMIN.getCode().equals(role) && status.equals(4)) return true;
        return false;
    }

    private int updateApplyStatusByOperate(StudentApply apply, Integer action, Integer role) {
        LambdaUpdateWrapper<StudentApply> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(StudentApply::getId, apply.getId())
                .eq(StudentApply::getStatus, apply.getStatus())
                .eq(StudentApply::getIsDeleted, 0);

        if (ReviewActionEnum.PASS.getCode().equals(action)) {
            if (role == 2) {  // 辅导员
                updateWrapper.set(StudentApply::getStatus, 3);
            } else if (role == 3) {  // 学院管理员
                updateWrapper.set(StudentApply::getStatus, 4);
            } else if (role == 4) {  // 学校管理员
                updateWrapper.set(StudentApply::getStatus, 5);
                // ⭐ 终审通过时，生成领取码
                String pickupCode = generatePickupCode(apply.getId());
                updateWrapper.set(StudentApply::getPickupCode, pickupCode);
            }
        } else if (ReviewActionEnum.REJECT_MODIFY.getCode().equals(action)
                || ReviewActionEnum.REJECT_NO_PASS.getCode().equals(action)) {
            updateWrapper.set(StudentApply::getStatus, 1);
        }
        updateWrapper.set(StudentApply::getUpdateTime, LocalDateTime.now());
        return studentApplyMapper.update(null, updateWrapper);
    }


     // 生成领取码
    private String generatePickupCode(Long applyId) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String suffix = timestamp.substring(timestamp.length() - 6);
        return "GIFT" + applyId + suffix;
    }
}

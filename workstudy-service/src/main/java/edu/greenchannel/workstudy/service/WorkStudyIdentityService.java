package edu.greenchannel.workstudy.service;

import edu.greenchannel.common.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 将认证域的用户 ID 映射为业务域的学生档案 ID。
 */
@Service
@RequiredArgsConstructor
public class WorkStudyIdentityService {

    private final JdbcTemplate jdbcTemplate;

    public long requireStudentId(long userId) {
        List<Long> studentIds = jdbcTemplate.queryForList("""
                SELECT id
                  FROM gc_student
                 WHERE user_id=? AND is_deleted=0
                 LIMIT 1
                """, Long.class, userId);
        if (studentIds.isEmpty()) {
            throw new BusinessException(40400, "当前账号未绑定学生档案");
        }
        return studentIds.get(0);
    }
}

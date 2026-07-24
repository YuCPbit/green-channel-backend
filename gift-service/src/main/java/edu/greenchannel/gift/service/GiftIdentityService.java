package edu.greenchannel.gift.service;

import edu.greenchannel.common.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GiftIdentityService {

    private final JdbcTemplate jdbcTemplate;

    public long requireStudentId(long userId) {
        List<Long> ids = jdbcTemplate.queryForList("""
                SELECT id FROM gc_student
                 WHERE user_id=? AND is_deleted=0
                 LIMIT 1
                """, Long.class, userId);
        if (ids.isEmpty()) {
            throw new BusinessException(40400, "当前账号未绑定学生档案");
        }
        return ids.get(0);
    }
}

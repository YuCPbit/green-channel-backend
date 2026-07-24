package edu.greenchannel.workstudy.service.impl;

import edu.greenchannel.workstudy.service.NotificationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class DatabaseNotificationServiceImpl implements NotificationService {
    private final JdbcTemplate jdbcTemplate;

    public DatabaseNotificationServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void sendWarning(Long receiverId, String title, String content, String businessKey) {
        insert(receiverId, "WORKSTUDY_WARNING", title, content, businessKey, "IMPORTANT");
    }

    @Override
    public void sendWarningToSchoolAdmins(String title, String content, String businessKey) {
        jdbcTemplate.queryForList("""
                SELECT id FROM gc_user
                WHERE user_type = 4 AND status = 1 AND is_deleted = 0
                """, Long.class).forEach(id -> sendWarning(id, title, content, businessKey));
    }

    @Override
    public void sendNotice(Long receiverId, String title, String content, String businessKey) {
        insert(receiverId, "WORKSTUDY_NOTICE", title, content, businessKey, "BUSINESS");
    }

    private void insert(Long receiverId, String eventCode, String title, String content,
            String businessKey, String messageType) {
        jdbcTemplate.update("""
                INSERT INTO gc_message
                  (receiver_user_id, event_code, business_id, title, content, message_type, read_status)
                VALUES (?, ?, ?, ?, ?, ?, 0)
                """, receiverId, eventCode, businessKey, title, content, messageType);
    }
}

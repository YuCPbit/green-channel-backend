package edu.greenchannel.workstudy.service.impl;

import edu.greenchannel.workstudy.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * 本地Mock消息服务
 * 作用：在消息中心未完成时，保证本模块功能正常。
 * @Profile("dev") : 只在 dev 环境下生效
 * @Primary : 当存在多个 NotificationService 实现时，优先用我。
 */
@Slf4j
@Service
@Primary
public class LocalMockNotificationServiceImpl implements NotificationService {

    @Override
    public void sendWarning(Long receiverId, String title, String content, String businessKey) {
        // 开发环境：只打印日志，不真正发消息
        log.warn("【Mock消息-预警】接收人ID:{}, 标题:{}, 内容:{}, 业务ID:{}",
                receiverId, title, content, businessKey);
    }

    @Override
    public void sendNotice(Long receiverId, String title, String content, String businessKey) {
        // 开发环境：只打印日志
        log.info("【Mock消息-通知】接收人ID:{}, 标题:{}, 内容:{}, 业务ID:{}",
                receiverId, title, content, businessKey);
    }
}

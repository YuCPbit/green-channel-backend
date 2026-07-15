package edu.greenchannel.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockChannelConfiguration {
    @Bean
    MessageChannelAdapter mockSmsAdapter() {
        return new LoggingMockAdapter("SMS");
    }

    @Bean
    MessageChannelAdapter mockWechatAdapter() {
        return new LoggingMockAdapter("WECHAT");
    }

    @Bean
    MessageChannelAdapter mockEmailAdapter() {
        return new LoggingMockAdapter("EMAIL");
    }

    private static final class LoggingMockAdapter implements MessageChannelAdapter {
        private static final Logger log = LoggerFactory.getLogger(LoggingMockAdapter.class);
        private final String channel;

        private LoggingMockAdapter(String channel) {
            this.channel = channel;
        }

        @Override
        public String channel() {
            return channel;
        }

        @Override
        public void send(MessageRecord message) {
            // 模拟适配器只记录消息主键，不输出接收人、标题或正文。
            log.info("Mock message delivery succeeded: channel={} messageId={}", channel, message.id());
        }
    }
}

package edu.greenchannel.message;

import edu.greenchannel.common.BusinessException;
import edu.greenchannel.common.PageResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageServiceTest {
    @Test
    void createsRenderedMessageAndDispatchesConfiguredChannels() {
        InMemoryRepository repository = new InMemoryRepository();
        RecordingAdapter sms = new RecordingAdapter("SMS", false);
        MessageService service = new MessageService(repository, List.of(sms));

        MessageRecord result = service.createFromEvent(new MessageEvent(
                "SUBSIDY_REVIEW_PASSED", 7, "A-100", Map.of("studentName", "测试学生")));

        assertEquals("审核通过-A-100", result.title());
        assertTrue(result.content().contains("测试学生"));
        assertEquals(List.of("IN_APP:SUCCESS", "SMS:SUCCESS"), repository.deliveries);
        assertEquals(1, sms.sent);
    }

    @Test
    void adapterFailureIsRecordedWithoutFailingSiteMessage() {
        InMemoryRepository repository = new InMemoryRepository();
        MessageService service = new MessageService(
                repository, List.of(new RecordingAdapter("SMS", true)));

        MessageRecord result = service.createFromEvent(new MessageEvent(
                "SUBSIDY_REVIEW_PASSED", 7, "A-101", Map.of("studentName", "测试学生")));

        assertEquals(1, result.id());
        assertEquals(List.of("IN_APP:SUCCESS", "SMS:RETRY"), repository.deliveries);
    }

    @Test
    void onlyOwnerCanReadAndMarkMessage() {
        InMemoryRepository repository = new InMemoryRepository();
        MessageService service = new MessageService(repository, List.of());
        service.createFromEvent(new MessageEvent(
                "SUBSIDY_REVIEW_PASSED", 7, "A-102", Map.of("studentName", "测试学生")));

        assertEquals(1, service.unreadCount(7));
        assertThrows(BusinessException.class, () -> service.markRead(1, 8));
        MessageRecord read = service.markRead(1, 7);
        assertTrue(read.read());
        assertEquals(0, service.unreadCount(7));
    }

    private static class RecordingAdapter implements MessageChannelAdapter {
        private final String channel;
        private final boolean fail;
        private int sent;

        private RecordingAdapter(String channel, boolean fail) {
            this.channel = channel;
            this.fail = fail;
        }

        @Override
        public String channel() {
            return channel;
        }

        @Override
        public void send(MessageRecord message) {
            if (fail) {
                throw new IllegalStateException("simulated");
            }
            sent++;
        }
    }

    private static class InMemoryRepository implements MessageRepository {
        private final List<MessageRecord> messages = new ArrayList<>();
        private final List<String> deliveries = new ArrayList<>();

        @Override
        public Optional<MessageTemplate> findTemplate(String eventCode) {
            return Optional.of(new MessageTemplate(
                    1, eventCode, "审核通过-${businessId}", "${studentName}的申请已通过",
                    "BUSINESS", List.of("SMS")));
        }

        @Override
        public MessageRecord insertMessage(MessageRecord message) {
            MessageRecord saved = new MessageRecord(
                    messages.size() + 1L, message.receiverUserId(), message.eventCode(), message.businessId(),
                    message.title(), message.content(), message.messageType(), false, null, LocalDateTime.now());
            messages.add(saved);
            return saved;
        }

        @Override
        public void saveDelivery(long messageId, String channel, String status, int attempts, String failureType) {
            deliveries.add(channel + ":" + status);
        }

        @Override
        public void saveEventRetry(MessageEvent event, Map<String, Object> variables, String failureType) {
        }

        @Override
        public PageResult<MessageRecord> search(
                long receiverUserId, Boolean readStatus, String messageType, int page, int size) {
            List<MessageRecord> items = messages.stream()
                    .filter(message -> message.receiverUserId() == receiverUserId)
                    .filter(message -> readStatus == null || message.read() == readStatus)
                    .toList();
            return new PageResult<>(items, items.size(), page, size);
        }

        @Override
        public long countUnread(long receiverUserId) {
            return messages.stream()
                    .filter(message -> message.receiverUserId() == receiverUserId && !message.read()).count();
        }

        @Override
        public Optional<MessageRecord> findByIdAndReceiver(long id, long receiverUserId) {
            return messages.stream()
                    .filter(message -> message.id() == id && message.receiverUserId() == receiverUserId)
                    .findFirst();
        }

        @Override
        public void markRead(long id, long receiverUserId) {
            for (int index = 0; index < messages.size(); index++) {
                MessageRecord value = messages.get(index);
                if (value.id() == id && value.receiverUserId() == receiverUserId) {
                    messages.set(index, new MessageRecord(
                            value.id(), value.receiverUserId(), value.eventCode(), value.businessId(),
                            value.title(), value.content(), value.messageType(), true,
                            LocalDateTime.now(), value.createdTime()));
                }
            }
        }

        @Override
        public int markAllRead(long receiverUserId) {
            int updated = 0;
            for (MessageRecord message : List.copyOf(messages)) {
                if (message.receiverUserId() == receiverUserId && !message.read()) {
                    markRead(message.id(), receiverUserId);
                    updated++;
                }
            }
            return updated;
        }
    }
}

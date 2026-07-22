package edu.greenchannel.message;

import edu.greenchannel.common.PageResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DefaultMessagePublisherTest {
    @Test
    void notificationFailureDoesNotEscapeToBusinessCaller() {
        RetryRepository repository = new RetryRepository();
        MessageService service = new MessageService(repository, List.of());
        DefaultMessagePublisher publisher = new DefaultMessagePublisher(service, repository);

        assertDoesNotThrow(() -> publisher.publish(
                new MessageEvent("UNKNOWN_EVENT", 7, "A-200", Map.of("value", "test"))));

        assertEquals(1, repository.retryCount);
        assertEquals("BusinessException", repository.failureType);
    }

    private static class RetryRepository implements MessageRepository {
        private int retryCount;
        private String failureType;

        @Override
        public Optional<MessageTemplate> findTemplate(String eventCode) {
            return Optional.empty();
        }

        @Override
        public MessageRecord insertMessage(MessageRecord message) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void saveDelivery(long messageId, String channel, String status, int attempts, String failureType) {
        }

        @Override
        public void saveEventRetry(MessageEvent event, Map<String, Object> variables, String failureType) {
            retryCount++;
            this.failureType = failureType;
        }

        @Override
        public PageResult<MessageRecord> search(
                long receiverUserId, Boolean readStatus, String messageType, int page, int size) {
            return new PageResult<>(List.of(), 0, page, size);
        }

        @Override
        public long countUnread(long receiverUserId) {
            return 0;
        }

        @Override
        public Optional<MessageRecord> findByIdAndReceiver(long id, long receiverUserId) {
            return Optional.empty();
        }

        @Override
        public void markRead(long id, long receiverUserId) {
        }

        @Override
        public int markAllRead(long receiverUserId) {
            return 0;
        }
    }
}

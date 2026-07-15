package edu.greenchannel.message;

import edu.greenchannel.common.PageResult;

import java.util.Map;
import java.util.Optional;

public interface MessageRepository {
    Optional<MessageTemplate> findTemplate(String eventCode);

    MessageRecord insertMessage(MessageRecord message);

    void saveDelivery(long messageId, String channel, String status, int attempts, String failureType);

    void saveEventRetry(MessageEvent event, Map<String, Object> variables, String failureType);

    PageResult<MessageRecord> search(long receiverUserId, Boolean readStatus, String messageType, int page, int size);

    long countUnread(long receiverUserId);

    Optional<MessageRecord> findByIdAndReceiver(long id, long receiverUserId);

    void markRead(long id, long receiverUserId);

    int markAllRead(long receiverUserId);
}

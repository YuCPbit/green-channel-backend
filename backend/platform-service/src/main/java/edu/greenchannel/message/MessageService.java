package edu.greenchannel.message;

import edu.greenchannel.common.BusinessException;
import edu.greenchannel.common.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MessageService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([A-Za-z0-9_]+)}");
    private final MessageRepository repository;
    private final Map<String, MessageChannelAdapter> adapters;

    public MessageService(MessageRepository repository, List<MessageChannelAdapter> adapters) {
        this.repository = repository;
        Map<String, MessageChannelAdapter> byChannel = new HashMap<>();
        for (MessageChannelAdapter adapter : adapters) {
            byChannel.put(adapter.channel().toUpperCase(Locale.ROOT), adapter);
        }
        this.adapters = Map.copyOf(byChannel);
    }

    public MessageRecord createFromEvent(MessageEvent event) {
        if (event == null || event.receiverUserId() <= 0) {
            throw new BusinessException(40001, "消息接收人不正确");
        }
        MessageEventType type = MessageEventType.from(event.eventType());
        MessageTemplate template = repository.findTemplate(type.name())
                .orElseThrow(() -> new BusinessException(40400, "消息模板不存在或未启用"));
        Map<String, Object> variables = new HashMap<>();
        if (event.variables() != null) {
            variables.putAll(event.variables());
        }
        variables.putIfAbsent("businessId", event.businessId());
        MessageRecord saved = repository.insertMessage(new MessageRecord(
                0, event.receiverUserId(), type.name(), event.businessId(),
                render(template.titleTemplate(), variables), render(template.contentTemplate(), variables),
                template.messageType(), false, null, LocalDateTime.now()));
        repository.saveDelivery(saved.id(), "IN_APP", "SUCCESS", 1, null);
        for (String channel : template.channels()) {
            dispatch(saved, channel);
        }
        return saved;
    }

    public PageResult<MessageRecord> search(
            long receiverUserId, Boolean readStatus, String messageType, int page, int size) {
        validatePage(page, size);
        String normalizedType = StringUtils.hasText(messageType)
                ? messageType.trim().toUpperCase(Locale.ROOT) : null;
        return repository.search(receiverUserId, readStatus, normalizedType, page, size);
    }

    public long unreadCount(long receiverUserId) {
        return repository.countUnread(receiverUserId);
    }

    public MessageRecord markRead(long id, long receiverUserId) {
        MessageRecord message = repository.findByIdAndReceiver(id, receiverUserId)
                .orElseThrow(() -> new BusinessException(40400, "消息不存在"));
        if (!message.read()) {
            repository.markRead(id, receiverUserId);
        }
        return repository.findByIdAndReceiver(id, receiverUserId).orElse(message);
    }

    public int markAllRead(long receiverUserId) {
        return repository.markAllRead(receiverUserId);
    }

    private void dispatch(MessageRecord message, String rawChannel) {
        String channel = rawChannel.toUpperCase(Locale.ROOT);
        if ("IN_APP".equals(channel)) {
            return;
        }
        MessageChannelAdapter adapter = adapters.get(channel);
        if (adapter == null) {
            repository.saveDelivery(message.id(), channel, "FAILED", 1, "AdapterNotFound");
            return;
        }
        try {
            adapter.send(message);
            repository.saveDelivery(message.id(), channel, "SUCCESS", 1, null);
        } catch (RuntimeException exception) {
            repository.saveDelivery(
                    message.id(), channel, "RETRY", 1, exception.getClass().getSimpleName());
        }
    }

    private String render(String template, Map<String, Object> variables) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            Object value = variables.get(matcher.group(1));
            if (value == null) {
                throw new BusinessException(40001, "消息模板变量缺失: " + matcher.group(1));
            }
            matcher.appendReplacement(output, Matcher.quoteReplacement(String.valueOf(value)));
        }
        matcher.appendTail(output);
        return output.toString();
    }

    private void validatePage(int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(40001, "分页参数不正确");
        }
    }
}

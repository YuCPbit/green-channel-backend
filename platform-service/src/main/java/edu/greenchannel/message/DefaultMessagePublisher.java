package edu.greenchannel.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;
import java.util.HashMap;

@Component
public class DefaultMessagePublisher implements MessagePublisher {
    private static final Logger log = LoggerFactory.getLogger(DefaultMessagePublisher.class);
    private final MessageService service;
    private final MessageRepository repository;

    public DefaultMessagePublisher(MessageService service, MessageRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @Override
    public void publish(MessageEvent event) {
        if (event == null) {
            log.warn("Ignored null message event");
            return;
        }
        Map<String, Object> variables = new HashMap<>();
        if (event.variables() != null) {
            event.variables().forEach((key, value) -> {
                if (key != null && value != null) {
                    variables.put(key, value);
                }
            });
        }
        MessageEvent safeEvent = new MessageEvent(
                event.eventType(), event.receiverUserId(), event.businessId(),
                Map.copyOf(variables));
        Runnable dispatch = () -> dispatchWithoutAffectingBusiness(safeEvent);
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    dispatch.run();
                }
            });
            return;
        }
        dispatch.run();
    }

    private void dispatchWithoutAffectingBusiness(MessageEvent event) {
        try {
            service.createFromEvent(event);
        } catch (RuntimeException exception) {
            log.warn("Message event dispatch failed: event={} failure={}",
                    event.eventType(), exception.getClass().getSimpleName());
            try {
                repository.saveEventRetry(event, event.variables(), exception.getClass().getSimpleName());
            } catch (RuntimeException retryException) {
                log.error("Message retry persistence failed: event={} failure={}",
                        event.eventType(), retryException.getClass().getSimpleName());
            }
        }
    }

}

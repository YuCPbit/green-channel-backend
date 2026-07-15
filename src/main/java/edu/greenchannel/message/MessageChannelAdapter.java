package edu.greenchannel.message;

public interface MessageChannelAdapter {
    String channel();

    void send(MessageRecord message);
}

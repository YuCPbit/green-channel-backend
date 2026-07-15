package edu.greenchannel.integration;

public record GreenChannelStatusRequest(
        String studentNo,
        String businessId,
        String status) {
}

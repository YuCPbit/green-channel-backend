package edu.greenchannel.integration;

public record SsoIdentity(boolean valid, String username, String provider) {
}

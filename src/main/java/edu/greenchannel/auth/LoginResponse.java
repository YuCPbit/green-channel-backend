package edu.greenchannel.auth;

public record LoginResponse(String token, long expiresInSeconds, CurrentUser user) {
}


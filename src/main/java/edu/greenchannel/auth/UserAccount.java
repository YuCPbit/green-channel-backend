package edu.greenchannel.auth;

public record UserAccount(
        long id,
        String username,
        String passwordHash,
        String realName,
        UserType userType,
        boolean enabled
) {
}


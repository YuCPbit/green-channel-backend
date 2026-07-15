package edu.greenchannel.auth;

import java.util.Optional;

public interface UserRepository {
    Optional<UserAccount> findByUsername(String username);

    void updateLastLoginTime(long userId);
}


package edu.greenchannel.auth;

import java.util.List;

public record CurrentUser(long id, String username, String realName, int userType,
                          String roleName, List<String> menus) {
    public static CurrentUser from(UserAccount account) {
        return new CurrentUser(
                account.id(), account.username(), account.realName(), account.userType().getCode(),
                account.userType().getDisplayName(), account.userType().getMenus()
        );
    }
}


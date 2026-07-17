package edu.greenchannel.auth;

import java.util.List;

public record CurrentUser(long id, String username, String realName, int userType,
                          Long collegeId, String roleName, List<String> roles,
                          List<String> permissions, List<String> menus) {
    public static CurrentUser from(UserAccount account) {
        String primaryRoleName = account.roleNames().isEmpty()
                ? account.userType().getDisplayName()
                : account.roleNames().get(0);
        return new CurrentUser(
                account.id(), account.username(), account.realName(), account.userType().getCode(),
                account.collegeId(),
                primaryRoleName, account.roleCodes(), account.permissionCodes(), account.menus()
        );
    }
}

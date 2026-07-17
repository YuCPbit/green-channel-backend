package edu.greenchannel.auth;

import java.util.List;

public record UserAccount(
        long id,
        String username,
        String passwordHash,
        String realName,
        Long collegeId,
        UserType userType,
        boolean enabled,
        List<String> roleCodes,
        List<String> roleNames,
        List<String> permissionCodes,
        List<String> menus
) {
}

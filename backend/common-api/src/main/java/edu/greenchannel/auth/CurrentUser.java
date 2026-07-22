package edu.greenchannel.auth;

import java.util.List;

public record CurrentUser(long id, String username, String realName, int userType,
                          String roleName, List<String> roles,
                          List<String> permissions, List<String> menus) {
}

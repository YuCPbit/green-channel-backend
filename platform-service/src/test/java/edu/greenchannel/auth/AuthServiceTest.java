package edu.greenchannel.auth;

import edu.greenchannel.common.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {
    private final InMemoryUserRepository repository = new InMemoryUserRepository();
    private final TokenService tokenService =
            new TokenService("unit-test-token-secret-at-least-32-characters");
    private final AuthService authService = new AuthService(repository, tokenService);

    @Test
    void shouldLoginAndReturnRoleMenus() {
        LoginResponse response = authService.login(new LoginRequest("student01", "Dev@123456"));

        assertEquals("学生", response.user().roleName());
        assertTrue(response.user().menus().contains("绿色通道"));
        assertTrue(tokenService.resolve(response.token()).isPresent());
    }

    @Test
    void shouldRejectWrongPassword() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login(new LoginRequest("student01", "wrong")));

        assertEquals(40100, exception.getCode());
    }

    @Test
    void shouldProvideDifferentMenusForFiveUserTypes() {
        Map<String, String> expectedRoles = Map.of(
                "student01", "学生",
                "tutor01", "辅导员",
                "college01", "学院管理员",
                "school01", "学校资助中心",
                "admin01", "系统管理员"
        );

        expectedRoles.forEach((username, roleName) -> {
            LoginResponse response = authService.login(new LoginRequest(username, "Dev@123456"));
            assertEquals(roleName, response.user().roleName());
            assertTrue(!response.user().menus().isEmpty());
        });
    }

    private static class InMemoryUserRepository implements UserRepository {
        private final String passwordHash = new BCryptPasswordEncoder().encode("Dev@123456");
        private final Map<String, UserAccount> users = Map.of(
                "student01", account(1L, "student01", "测试学生", UserType.STUDENT),
                "tutor01", account(2L, "tutor01", "测试辅导员", UserType.TUTOR),
                "college01", account(3L, "college01", "测试学院管理员", UserType.COLLEGE_ADMIN),
                "school01", account(4L, "school01", "测试资助中心", UserType.SCHOOL_ADMIN),
                "admin01", account(5L, "admin01", "测试系统管理员", UserType.SYSTEM_ADMIN)
        );

        @Override
        public Optional<UserAccount> findByUsername(String username) {
            return Optional.ofNullable(users.get(username));
        }

        @Override
        public void updateLastLoginTime(long userId) {
            // 单元测试无需持久化时间。
        }

        private UserAccount account(long id, String username, String realName, UserType userType) {
            return new UserAccount(
                    id, username, passwordHash, realName, userType, true,
                    List.of(userType.name()), List.of(userType.getDisplayName()),
                    List.of(userType == UserType.SYSTEM_ADMIN ? "system:rbac:view" : "home:view"),
                    userType.getMenus()
            );
        }
    }
}

package edu.greenchannel.auth;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenServiceTest {
    private static final String TEST_SECRET = "unit-test-token-secret-at-least-32-characters";

    @Test
    void tokenCanBeVerifiedByAnotherServiceInstance() {
        TokenService issuer = new TokenService(TEST_SECRET);
        TokenService verifier = new TokenService(TEST_SECRET);
        CurrentUser user = new CurrentUser(
                7L, "student07", "测试学生", 1, "学生",
                List.of("STUDENT"), List.of("student:subsidy:view"), List.of("困难补助"));

        CurrentUser resolved = verifier.resolve(issuer.issue(user)).orElseThrow();

        assertEquals(user, resolved);
    }

    @Test
    void tamperedTokenIsRejected() {
        TokenService service = new TokenService(TEST_SECRET);
        CurrentUser user = new CurrentUser(
                7L, "student07", "测试学生", 1, "学生",
                List.of("STUDENT"), List.of(), List.of());
        String token = service.issue(user);
        String tampered = (token.charAt(0) == 'A' ? 'B' : 'A') + token.substring(1);

        assertTrue(service.resolve(tampered).isEmpty());
    }
}

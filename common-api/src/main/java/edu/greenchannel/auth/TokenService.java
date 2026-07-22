package edu.greenchannel.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class TokenService {
    public static final Duration TOKEN_TTL = Duration.ofHours(8);
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String DEFAULT_DEV_SECRET = "green-channel-dev-secret-change-before-deploy";

    private final byte[] secret;

    public TokenService() {
        this(System.getenv().getOrDefault("APP_AUTH_TOKEN_SECRET", DEFAULT_DEV_SECRET));
    }

    @Autowired
    public TokenService(@Value("${app.auth.token-secret:${APP_AUTH_TOKEN_SECRET:" + DEFAULT_DEV_SECRET + "}}") String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("认证令牌密钥至少需要32个字符");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String issue(CurrentUser user) {
        byte[] payload = encode(user, Instant.now().plus(TOKEN_TTL));
        return encodeUrl(payload) + "." + encodeUrl(sign(payload));
    }

    public Optional<CurrentUser> resolve(String token) {
        try {
            if (token == null) {
                return Optional.empty();
            }
            String[] parts = token.split("\\.", -1);
            if (parts.length != 2) {
                return Optional.empty();
            }
            byte[] payload = Base64.getUrlDecoder().decode(parts[0]);
            byte[] suppliedSignature = Base64.getUrlDecoder().decode(parts[1]);
            if (!MessageDigest.isEqual(sign(payload), suppliedSignature)) {
                return Optional.empty();
            }
            return decode(payload);
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    public void revoke(String token) {
        // 签名令牌无服务端会话；客户端丢弃令牌，最迟在 TOKEN_TTL 后自然失效。
    }

    private byte[] encode(CurrentUser user, Instant expiresAt) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(buffer)) {
                output.writeLong(expiresAt.getEpochSecond());
                output.writeLong(user.id());
                output.writeInt(user.userType());
                writeString(output, user.username());
                writeString(output, user.realName());
                writeString(output, user.roleName());
                writeList(output, user.roles());
                writeList(output, user.permissions());
                writeList(output, user.menus());
            }
            return buffer.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("无法生成认证令牌", exception);
        }
    }

    private Optional<CurrentUser> decode(byte[] payload) {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(payload))) {
            Instant expiresAt = Instant.ofEpochSecond(input.readLong());
            if (!expiresAt.isAfter(Instant.now())) {
                return Optional.empty();
            }
            long id = input.readLong();
            int userType = input.readInt();
            String username = input.readUTF();
            String realName = input.readUTF();
            String roleName = input.readUTF();
            List<String> roles = readList(input);
            List<String> permissions = readList(input);
            List<String> menus = readList(input);
            if (input.available() != 0) {
                return Optional.empty();
            }
            return Optional.of(new CurrentUser(id, username, realName, userType,
                    roleName, roles, permissions, menus));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private byte[] sign(byte[] payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return mac.doFinal(payload);
        } catch (Exception exception) {
            throw new IllegalStateException("无法签名认证令牌", exception);
        }
    }

    private static void writeString(DataOutputStream output, String value) throws Exception {
        output.writeUTF(value == null ? "" : value);
    }

    private static void writeList(DataOutputStream output, List<String> values) throws Exception {
        List<String> safeValues = values == null ? List.of() : values;
        output.writeInt(safeValues.size());
        for (String value : safeValues) {
            writeString(output, value);
        }
    }

    private static List<String> readList(DataInputStream input) throws Exception {
        int size = input.readInt();
        if (size < 0 || size > 1_000) {
            throw new IllegalArgumentException("无效令牌列表长度");
        }
        List<String> values = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            values.add(input.readUTF());
        }
        return List.copyOf(values);
    }

    private static String encodeUrl(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}

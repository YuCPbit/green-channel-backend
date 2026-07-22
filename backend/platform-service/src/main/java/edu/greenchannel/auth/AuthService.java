package edu.greenchannel.auth;

import edu.greenchannel.common.BusinessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            throw new BusinessException(40001, "用户名和密码不能为空");
        }
        UserAccount account = userRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new BusinessException(40100, "用户名或密码错误"));
        if (!account.enabled()) {
            throw new BusinessException(40100, "账号已被禁用");
        }
        if (!passwordEncoder.matches(request.password(), account.passwordHash())) {
            throw new BusinessException(40100, "用户名或密码错误");
        }
        String primaryRoleName = account.roleNames().isEmpty()
                ? account.userType().getDisplayName()
                : account.roleNames().get(0);
        CurrentUser currentUser = new CurrentUser(
                account.id(), account.username(), account.realName(), account.userType().getCode(),
                primaryRoleName, account.roleCodes(), account.permissionCodes(), account.menus()
        );
        String token = tokenService.issue(currentUser);
        userRepository.updateLastLoginTime(account.id());
        return new LoginResponse(token, TokenService.TOKEN_TTL.toSeconds(), currentUser);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

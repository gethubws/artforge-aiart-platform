package com.aiart.platform.service.impl;

import com.aiart.platform.dto.AuthDtos;
import com.aiart.platform.entity.PointAccount;
import com.aiart.platform.entity.User;
import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;
import com.aiart.platform.mapper.PointAccountMapper;
import com.aiart.platform.mapper.UserMapper;
import com.aiart.platform.security.JwtService;
import com.aiart.platform.service.AuthService;
import com.aiart.platform.service.LoginAttemptService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final PointAccountMapper pointAccountMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;

    @Override
    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        return createUser(request, "USER");
    }

    @Override
    @Transactional
    public AuthDtos.AuthResponse bootstrapAdmin(AuthDtos.RegisterRequest request) {
        Long adminCount = userMapper.selectCount(Wrappers.<User>lambdaQuery().eq(User::getRole, "ADMIN"));
        if (adminCount > 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Admin account already exists");
        }
        return createUser(request, "ADMIN");
    }

    private AuthDtos.AuthResponse createUser(AuthDtos.RegisterRequest request, String role) {
        Long existing = userMapper.selectCount(Wrappers.<User>lambdaQuery().eq(User::getUsername, request.username()));
        if (existing > 0) {
            throw new BusinessException(ErrorCode.USER_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(request.username().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName().trim());
        user.setRole(role);
        user.setStatus("ACTIVE");
        user.setPointBalance(BigDecimal.ZERO);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);

        PointAccount account = new PointAccount();
        account.setUserId(user.getId());
        account.setBalance(BigDecimal.ZERO);
        account.setFrozenBalance(BigDecimal.ZERO);
        account.setCreatedAt(now);
        account.setUpdatedAt(now);
        pointAccountMapper.insert(account);

        return issue(user);
    }

    @Override
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        loginAttemptService.checkAllowed(request.username());
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, request.username()));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            loginAttemptService.recordFailure(request.username());
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "User account is not active");
        }
        loginAttemptService.clear(request.username());
        return issue(user);
    }

    @Override
    public AuthDtos.UserProfile currentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "User not found");
        }
        return profile(user);
    }

    private AuthDtos.AuthResponse issue(User user) {
        String token = jwtService.createToken(user.getId(), user.getUsername(), user.getRole());
        return new AuthDtos.AuthResponse(token, profile(user));
    }

    private AuthDtos.UserProfile profile(User user) {
        return new AuthDtos.UserProfile(user.getId(), user.getUsername(), user.getDisplayName(), user.getAvatarUrl(), user.getRole());
    }
}

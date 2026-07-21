package com.aiart.platform.service.impl;

import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;
import com.aiart.platform.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {
    private static final String KEY_PREFIX = "security:login-attempts:";

    private final StringRedisTemplate redisTemplate;

    @Value("${aiart.security.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${aiart.security.login.window-seconds:900}")
    private long windowSeconds;

    @Override
    public void checkAllowed(String username) {
        try {
            String value = redisTemplate.opsForValue().get(key(username));
            if (value != null && Long.parseLong(value) >= maxAttempts) {
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS,
                        "Too many failed login attempts. Try again later");
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.warn("Login rate-limit check is unavailable: {}", ex.getMessage());
        }
    }

    @Override
    public void recordFailure(String username) {
        try {
            String key = key(username);
            Long attempts = redisTemplate.opsForValue().increment(key);
            if (attempts != null && attempts == 1L) {
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            }
        } catch (RuntimeException ex) {
            log.warn("Unable to record failed login attempt: {}", ex.getMessage());
        }
    }

    @Override
    public void clear(String username) {
        try {
            redisTemplate.delete(key(username));
        } catch (RuntimeException ex) {
            log.warn("Unable to clear login attempt counter: {}", ex.getMessage());
        }
    }

    private String key(String username) {
        String normalized = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(normalized.getBytes(StandardCharsets.UTF_8));
            return KEY_PREFIX + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}

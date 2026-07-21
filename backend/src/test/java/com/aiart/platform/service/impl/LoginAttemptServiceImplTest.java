package com.aiart.platform.service.impl;

import com.aiart.platform.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginAttemptServiceImplTest {
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private LoginAttemptServiceImpl service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new LoginAttemptServiceImpl(redisTemplate);
        ReflectionTestUtils.setField(service, "maxAttempts", 5);
        ReflectionTestUtils.setField(service, "windowSeconds", 900L);
    }

    @Test
    void blocksUsernameAfterMaximumFailures() {
        when(valueOperations.get(anyString())).thenReturn("5");

        assertThrows(BusinessException.class, () -> service.checkAllowed("artist"));
    }

    @Test
    void startsExpirationWindowOnFirstFailure() {
        when(valueOperations.increment(anyString())).thenReturn(1L);

        service.recordFailure("artist");

        verify(redisTemplate).expire(anyString(), org.mockito.ArgumentMatchers.eq(Duration.ofSeconds(900)));
    }

    @Test
    void failsOpenWhenRedisIsUnavailable() {
        when(valueOperations.get(anyString())).thenThrow(new IllegalStateException("redis unavailable"));

        assertDoesNotThrow(() -> service.checkAllowed("artist"));
    }
}

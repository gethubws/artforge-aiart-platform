package com.aiart.platform.service;

public interface LoginAttemptService {
    void checkAllowed(String username);

    void recordFailure(String username);

    void clear(String username);
}

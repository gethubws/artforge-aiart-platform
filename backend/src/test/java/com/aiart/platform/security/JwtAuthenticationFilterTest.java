package com.aiart.platform.security;

import com.aiart.platform.entity.User;
import com.aiart.platform.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {
    private JwtService jwtService;
    private UserMapper userMapper;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "test-secret-that-is-longer-than-thirty-two-bytes");
        ReflectionTestUtils.setField(jwtService, "expirationSeconds", 3600L);
        userMapper = mock(UserMapper.class);
        filter = new JwtAuthenticationFilter(jwtService, userMapper);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void establishesAuthenticationForActiveUser() throws Exception {
        User user = user("ACTIVE", "ADMIN");
        when(userMapper.selectById(7L)).thenReturn(user);
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = request(jwtService.createToken(7L, "admin", "ADMIN"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals("7", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertEquals("ROLE_ADMIN", SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .iterator().next().getAuthority());
        verify(chain).doFilter(request, response);
    }

    @Test
    void rejectsTokenWhenUserIsSuspended() throws Exception {
        when(userMapper.selectById(7L)).thenReturn(user("SUSPENDED", "USER"));
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = request(jwtService.createToken(7L, "member", "USER"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    @Test
    void ignoresTamperedToken() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = request(jwtService.createToken(7L, "member", "USER") + "tampered");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(request, response);
    }

    private MockHttpServletRequest request(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/account");
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private User user(String status, String role) {
        User user = new User();
        user.setId(7L);
        user.setStatus(status);
        user.setRole(role);
        return user;
    }
}

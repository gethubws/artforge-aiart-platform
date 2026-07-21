package com.aiart.platform.config;

import com.aiart.platform.security.CurrentUser;
import com.aiart.platform.service.AdminOperationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminOperationInterceptor implements HandlerInterceptor {
    private static final Pattern USER_UPDATE = Pattern.compile("^/api/admin/users/(\\d+)$");
    private static final Pattern AUDIT_REVIEW = Pattern.compile("^/api/audits/(\\d+)/review$");
    private static final Pattern TAG_ITEM = Pattern.compile("^/api/tags/(\\d+)$");
    private static final Pattern TAG_PREVIEW = Pattern.compile("^/api/tags/(\\d+)/previews/(\\d+)$");
    private static final Pattern TAG_PREVIEW_IMAGE = Pattern.compile("^/api/tags/(\\d+)/previews/(\\d+)/image$");
    private static final Pattern TAG_PREVIEW_ORDER = Pattern.compile("^/api/tags/(\\d+)/previews/order$");
    private static final Pattern TAG_PREVIEW_CREATE = Pattern.compile("^/api/tags/(\\d+)/previews$");

    private final AdminOperationService adminOperationService;
    private final CurrentUser currentUser;

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        if (ex != null || response.getStatus() < 200 || response.getStatus() >= 300 || !isAdmin()) {
            return;
        }
        Descriptor descriptor = describe(request.getMethod(), request.getRequestURI());
        if (descriptor == null) {
            return;
        }
        Long operatorId = currentUser.userIdOrNull();
        if (operatorId == null) {
            return;
        }
        try {
            adminOperationService.record(
                    operatorId,
                    descriptor.action(),
                    descriptor.targetType(),
                    descriptor.targetId(),
                    request.getMethod(),
                    request.getRequestURI(),
                    descriptor.summary(),
                    clientIp(request));
        } catch (RuntimeException logError) {
            log.warn("Unable to persist administrator operation log", logError);
        }
    }

    private Descriptor describe(String method, String path) {
        Matcher matcher = USER_UPDATE.matcher(path);
        if ("PUT".equals(method) && matcher.matches()) {
            return descriptor("USER_UPDATE", "USER", matcher.group(1), "更新用户资料、角色或状态");
        }
        matcher = AUDIT_REVIEW.matcher(path);
        if ("POST".equals(method) && matcher.matches()) {
            return descriptor("CONTENT_AUDIT_REVIEW", "CONTENT_AUDIT", matcher.group(1), "完成内容审核");
        }
        if ("POST".equals(method) && "/api/tags/categories".equals(path)) {
            return new Descriptor("TAG_CATEGORY_CREATE", "TAG_CATEGORY", null, "创建标签分类");
        }
        if ("POST".equals(method) && "/api/tags".equals(path)) {
            return new Descriptor("TAG_CREATE", "TAG", null, "创建提示词标签");
        }
        matcher = TAG_PREVIEW_ORDER.matcher(path);
        if ("PUT".equals(method) && matcher.matches()) {
            return descriptor("TAG_PREVIEW_REORDER", "TAG", matcher.group(1), "调整标签预览图顺序");
        }
        matcher = TAG_PREVIEW_IMAGE.matcher(path);
        if ("POST".equals(method) && matcher.matches()) {
            return descriptor("TAG_PREVIEW_REPLACE", "TAG_PREVIEW", matcher.group(2), "替换标签预览图文件");
        }
        matcher = TAG_PREVIEW.matcher(path);
        if (matcher.matches()) {
            if ("PUT".equals(method)) {
                return descriptor("TAG_PREVIEW_UPDATE", "TAG_PREVIEW", matcher.group(2), "更新标签预览图信息");
            }
            if ("DELETE".equals(method)) {
                return descriptor("TAG_PREVIEW_DELETE", "TAG_PREVIEW", matcher.group(2), "删除标签预览图");
            }
        }
        matcher = TAG_PREVIEW_CREATE.matcher(path);
        if ("POST".equals(method) && matcher.matches()) {
            return descriptor("TAG_PREVIEW_CREATE", "TAG", matcher.group(1), "上传标签预览图");
        }
        matcher = TAG_ITEM.matcher(path);
        if (matcher.matches()) {
            if ("PUT".equals(method)) {
                return descriptor("TAG_UPDATE", "TAG", matcher.group(1), "更新提示词标签");
            }
            if ("DELETE".equals(method)) {
                return descriptor("TAG_DEACTIVATE", "TAG", matcher.group(1), "停用提示词标签");
            }
        }
        return null;
    }

    private Descriptor descriptor(String action, String targetType, String id, String summary) {
        return new Descriptor(action, targetType, Long.valueOf(id), summary);
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",", 2)[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record Descriptor(String action, String targetType, Long targetId, String summary) {
    }
}

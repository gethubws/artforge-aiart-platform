package com.aiart.platform.storage;

import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public final class StorageReferenceCodec {
    private static final String REFERENCE_PREFIX = "storage://";
    private static final String PUBLIC_URL_PREFIX = "/api/public/files/";

    private StorageReferenceCodec() {
    }

    public static String reference(boolean publicObject, String key) {
        String visibility = publicObject ? "public/" : "private/";
        return REFERENCE_PREFIX + visibility + normalizeKey(key);
    }

    public static boolean isManaged(String value) {
        return value != null && value.startsWith(REFERENCE_PREFIX);
    }

    public static boolean isPublic(String reference) {
        return reference != null && reference.startsWith(REFERENCE_PREFIX + "public/");
    }

    public static String objectName(String reference) {
        if (!isManaged(reference)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported storage reference");
        }
        return normalizeKey(reference.substring(REFERENCE_PREFIX.length()));
    }

    public static String publicUrl(String reference) {
        if (!isPublic(reference)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Private objects cannot have public URLs");
        }
        String token = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(reference.getBytes(StandardCharsets.UTF_8));
        return PUBLIC_URL_PREFIX + token;
    }

    public static String referenceFromPublicToken(String token) {
        try {
            String reference = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            if (!isPublic(reference)) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "Only public objects can be accessed here");
            }
            return reference;
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid file token");
        }
    }

    public static String referenceFromPublicUrl(String url) {
        if (url == null || !url.startsWith(PUBLIC_URL_PREFIX)) {
            return null;
        }
        return referenceFromPublicToken(url.substring(PUBLIC_URL_PREFIX.length()));
    }

    private static String normalizeKey(String value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Storage key is required");
        }
        String normalized = value.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        boolean hasParentSegment = Arrays.stream(normalized.split("/"))
                .anyMatch(".."::equals);
        if (normalized.isBlank() || hasParentSegment) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid storage key");
        }
        return normalized;
    }
}

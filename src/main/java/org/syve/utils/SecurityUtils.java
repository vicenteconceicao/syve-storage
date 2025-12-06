package org.syve.utils;

import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityUtils.class);

    private static final Pattern SAFE_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");

    private static final Pattern DANGEROUS_PATTERNS = Pattern.compile(
        "(\\.\\./|\\.\\.\\\\|/|\\\\|%2e%2e|%2E%2E|%2f|%2F|%5c|%5C)", 
        Pattern.CASE_INSENSITIVE
    );

    private static final int MAX_BUCKET_NAME_LENGTH = 63;
    private static final int MAX_FILE_NAME_LENGTH = 255;

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp",
        "image/svg+xml",
        "application/pdf"
    );

    public static String validateBucketName(String bucketName) {
        if (bucketName == null || bucketName.isBlank()) {
            LOG.warn("Bucket name is null or empty");
            throw new IllegalArgumentException("Bucket name cannot be null or empty");
        }

        String trimmed = bucketName.trim();

        if (trimmed.length() > MAX_BUCKET_NAME_LENGTH) {
            LOG.warn("Bucket name too long: {}", trimmed);
            throw new IllegalArgumentException("Bucket name exceeds maximum length");
        }

        if (DANGEROUS_PATTERNS.matcher(trimmed).find()) {
            LOG.warn("Bucket name contains dangerous patterns: {}", trimmed);
            throw new IllegalArgumentException("Bucket name contains invalid characters");
        }

        if (!SAFE_NAME_PATTERN.matcher(trimmed).matches()) {
            LOG.warn("Bucket name contains invalid characters: {}", trimmed);
            throw new IllegalArgumentException("Bucket name contains invalid characters");
        }

        return trimmed;
    }

    public static String validateFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            LOG.warn("File name is null or empty");
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

        String trimmed = fileName.trim();

        if (trimmed.length() > MAX_FILE_NAME_LENGTH) {
            LOG.warn("File name too long: {}", trimmed);
            throw new IllegalArgumentException("File name exceeds maximum length");
        }

        if (DANGEROUS_PATTERNS.matcher(trimmed).find()) {
            LOG.warn("File name contains dangerous patterns: {}", trimmed);
            throw new IllegalArgumentException("File name contains invalid characters");
        }

        if (!SAFE_NAME_PATTERN.matcher(trimmed).matches()) {
            LOG.warn("File name contains invalid characters: {}", trimmed);
            throw new IllegalArgumentException("File name contains invalid characters");
        }

        return trimmed;
    }

    public static String sanitizeFileNameForHeader(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "file";
        }

        String sanitized = fileName.replaceAll("[\\x00-\\x1F\\x7F]", "");
        
        sanitized = sanitized.replaceAll("[\\r\\n]", "");
        
        sanitized = sanitized.replaceAll("[;\"\\\\]", "_");
        
        if (sanitized.length() > 200) {
            sanitized = sanitized.substring(0, 200);
        }

        if (sanitized.isBlank()) {
            return "file";
        }

        return sanitized;
    }

    public static boolean isAllowedMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return false;
        }
        
        String baseMimeType = mimeType.split(";")[0].trim().toLowerCase();
        
        return ALLOWED_MIME_TYPES.contains(baseMimeType);
    }

    public static boolean isValidFileSize(long fileSize, long maxSize) {
        return fileSize > 0 && fileSize <= maxSize;
    }
}


package com.nect.api.domain.team.chat.util;

import com.nect.api.global.code.StorageErrorCode;
import com.nect.api.global.infra.exception.StorageException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;


public class FileValidator {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            ".jpg",
            ".jpeg",
            ".png"
    );

    public static boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        if (filename == null) return false;

        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();

        return ALLOWED_IMAGE_TYPES.contains(contentType)
                && ALLOWED_IMAGE_EXTENSIONS.contains(extension);
    }

    public static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException(StorageErrorCode.EMPTY_FILE);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new StorageException(StorageErrorCode.FILE_UPLOAD_FAILED);
        }

        // 필요시 특정 파일 타입 차단
        String contentType = file.getContentType();
        if (contentType != null && isBlockedFileType(contentType)) {
            throw new StorageException(StorageErrorCode.INVALID_FILE_TYPE);
        }
    }

    private static boolean isBlockedFileType(String contentType) {
        // 실행 파일 등 위험한 파일 차단
        List<String> blockedTypes = Arrays.asList(
                "application/x-msdownload",
                "application/x-sh",
                "application/x-executable"
        );
        return blockedTypes.contains(contentType);
    }
}
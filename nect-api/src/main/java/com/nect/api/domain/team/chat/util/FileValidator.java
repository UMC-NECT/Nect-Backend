package com.nect.api.domain.team.chat.util;

import com.nect.api.global.infra.exception.StorageErrorCode;
import com.nect.api.global.infra.exception.StorageException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class FileValidator {

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

    public static void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new StorageException(StorageErrorCode.EMPTY_FILE);
        }
        if (!isImage(file)) {
            throw new StorageException(StorageErrorCode.INVALID_FILE_TYPE);
        }
    }
}
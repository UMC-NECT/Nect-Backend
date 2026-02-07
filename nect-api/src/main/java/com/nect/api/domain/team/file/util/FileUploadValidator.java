package com.nect.api.domain.team.file.util;

import com.nect.api.domain.team.file.enums.FileErrorCode;
import com.nect.api.domain.team.file.exception.FileException;
import com.nect.core.entity.team.enums.FileExt;
import org.springframework.web.multipart.MultipartFile;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public final class FileUploadValidator {
    private static final long MB = 1024L * 1024L;

    private static final long MAX_5MB = 5L * MB;
    private static final long MAX_20MB = 20L * MB;

    private static final Set<FileExt> LIMIT_5MB = EnumSet.of(FileExt.JPG, FileExt.PNG, FileExt.SVG);
    private static final Set<FileExt> LIMIT_20MB = EnumSet.of(FileExt.PDF, FileExt.DOCS, FileExt.PPTX, FileExt.FIG, FileExt.ZIP);

    private FileUploadValidator() {
    }

    public static void validateNotEmpty(MultipartFile file) {
        if (file == null) {
            throw new FileException(FileErrorCode.INVALID_REQUEST, "file is null");
        }
        if (file.isEmpty()) {
            throw new FileException(FileErrorCode.EMPTY_FILE, "file is empty");
        }
    }

    public static void validateSizeOrThrow(FileExt ext, long fileSize) {
        long max;

        if (LIMIT_5MB.contains(ext)) {
            max = MAX_5MB;
        } else if (LIMIT_20MB.contains(ext)) {
            max = MAX_20MB;
        } else {
            throw new FileException(FileErrorCode.UNSUPPORTED_FILE_EXT, "fileExt = " + ext);
        }

        if (fileSize > max) {
            throw new FileException(FileErrorCode.FILE_SIZE_EXCEEDED, "fileExt = " + ext + ", size = " + fileSize + ", max = " + max);
        }
    }

    public static FileExt resolveExtOrThrow(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        int dot = lower.lastIndexOf('.');
        String ext = (dot >= 0) ? lower.substring(dot + 1) : "";

        return switch (ext) {
            case "jpg", "jpeg" -> FileExt.JPG;
            case "png" -> FileExt.PNG;
            case "svg" -> FileExt.SVG;
            case "pdf" -> FileExt.PDF;
            case "docs" -> FileExt.DOCS;
            case "pptx" -> FileExt.PPTX;
            case "fig" -> FileExt.FIG;
            case "zip" -> FileExt.ZIP;
            default -> throw new FileException(FileErrorCode.UNSUPPORTED_FILE_EXT, "fileName=" + fileName);
        };
    }
}

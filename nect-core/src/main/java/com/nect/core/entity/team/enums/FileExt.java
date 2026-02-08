package com.nect.core.entity.team.enums;

import java.util.Locale;

public enum FileExt {
    JPG, PNG, SVG, PDF, DOCS, PPTX, FIG, ZIP;

    public static FileExt fromFilename(String fileName) {
        if (fileName == null) {
            return null;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        int dot = lower.lastIndexOf('.');
        String ext = (dot >= 0) ? lower.substring(dot + 1) : "";

        return switch (ext) {
            case "jpg", "jpeg" -> JPG;
            case "png" -> PNG;
            case "svg" -> SVG;
            case "pdf" -> PDF;
            case "docs" -> DOCS;
            case "pptx" -> PPTX;
            case "fig" -> FIG;
            case "zip" -> ZIP;
            default -> null;
        };
    }
}

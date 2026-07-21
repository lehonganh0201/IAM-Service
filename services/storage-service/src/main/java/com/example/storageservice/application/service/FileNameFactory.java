package com.example.storageservice.application.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:47
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
public class FileNameFactory {
    public String extensionOf(String f) {
        if (!StringUtils.hasText(f) || !f.contains(".")) return "bin";
        return f.substring(f.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    public String storedName(String f) {
        return UUID.randomUUID() + "." + extensionOf(f);
    }

    public String objectKey(String f) {
        LocalDate n = LocalDate.now();
        return "%d/%02d/%02d/%s".formatted(n.getYear(), n.getMonthValue(), n.getDayOfMonth(), storedName(f));
    }

    public String sanitize(String f) {
        if (!StringUtils.hasText(f)) return "file";
        return Normalizer.normalize(f, Normalizer.Form.NFD).replaceAll("[\\p{InCombiningDiacriticalMarks}]", "").replaceAll("[\\\\/]+", "_").replaceAll("[^a-zA-Z0-9._ -]", "_");
    }
}


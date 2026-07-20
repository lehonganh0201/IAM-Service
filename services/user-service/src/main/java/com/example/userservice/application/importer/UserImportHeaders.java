package com.example.userservice.application.importer;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 16:43
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public final class UserImportHeaders {
    private UserImportHeaders() {
    }

    public static final List<String> REQUIRED = List.of("STT", "username", "Họ Tên", "Ngày sinh", "Tên đường", "Xã (Phường)", "Huyện", "Tỉnh", "Số năm kinh nghiệm");
}


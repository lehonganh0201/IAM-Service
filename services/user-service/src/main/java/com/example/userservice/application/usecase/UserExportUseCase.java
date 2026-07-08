package com.example.userservice.application.usecase;

import com.example.commonlib.exception.BadRequestException;
import com.example.userservice.application.dto.request.UserSearchQuery;
import com.example.userservice.application.exporter.UserExportStrategy;
import com.example.userservice.infrastructure.persistence.UserRepository;
import com.example.userservice.infrastructure.persistence.UserSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 17:04
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
public class UserExportUseCase {
    private final UserRepository userRepository;
    private final List<UserExportStrategy> strategies;

    public ExportedFile export(String format, UserSearchQuery q) {
        var st = strategies.stream().filter(x -> x.format().equalsIgnoreCase(format))
                .findFirst().orElseThrow(() -> new BadRequestException("Unsupported export format"));

        var users = userRepository.findAll(UserSpecifications.byQuery(q));
        return new ExportedFile(st.filename(), st.contentType(), st.export(users));
    }

    public record ExportedFile(String filename, String contentType, byte[] bytes) {
    }
}

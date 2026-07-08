package com.example.iamservice.service.exporter;

import com.example.commonlib.exception.BadRequestException;
import com.example.iamservice.domain.dto.request.UserSearchQuery;
import com.example.iamservice.repository.UserRepository;
import com.example.iamservice.repository.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    08/07/2026 at 11:57
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
public class ExportService {
    private final UserRepository userRepository;
    private final List<UserExportStrategy> strategies;

    public ExportService.ExportedFile export(String format, UserSearchQuery q) {
        var st = strategies.stream().filter(x -> x.format().equalsIgnoreCase(format))
                .findFirst().orElseThrow(() -> new BadRequestException("Unsupported export format"));

        var users = userRepository.findAll(UserSpecification.byQuery(q));
        return new ExportService.ExportedFile(st.filename(), st.contentType(), st.export(users));
    }

    public record ExportedFile(String filename, String contentType, byte[] bytes) {
    }
}

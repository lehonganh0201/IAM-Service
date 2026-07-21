package com.example.iamservice.service.exporter;

import com.example.commonlib.exception.BadRequestException;
import com.example.iamservice.domain.entity.User;
import com.example.iamservice.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    08/07/2026 at 12:03
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
@RequiredArgsConstructor
public class CsvUserExportStrategy implements UserExportStrategy {
    static final String[] H = {"STT", "username", "Họ Tên", "Ngày sinh", "Tên đường", "Xã (Phường)", "Huyện", "Tỉnh", "Số năm kinh nghiệm", "Ngày tạo", "Trạng thái"};
    static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final UserProfileRepository profileRepository;

    public String format() {
        return "csv";
    }

    public String contentType() {
        return "text/csv; charset=UTF-8";
    }

    public String filename() {
        return "users.csv";
    }

    public byte[] export(List<User> users) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);
            try (CSVPrinter p = new CSVPrinter(new OutputStreamWriter(out, StandardCharsets.UTF_8),
                    CSVFormat.DEFAULT.builder().setHeader(H).build())) {
                int i = 1;
                for (var u : users) {
                    var profile = profileRepository.findById(u.getId()).orElse(null);
                    p.printRecord(i++,
                            u.getUsername(),
                            u.getDisplayName(),
                            u.getDateOfBirth() == null ? "" : D.format(u.getDateOfBirth()),
                            profile == null ? "" : profile.getStreet(),
                            profile == null ? "" : profile.getWard(),
                            profile == null ? "" : profile.getDistrict(),
                            profile == null ? "" : profile.getProvince(),
                            profile == null ? "" : profile.getYearsOfExperience(),
                            u.getCreatedAt() == null ? "" : D.format(u.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate()),
                            u.isActive() ? "Active" : "Inactive");
                }
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new BadRequestException("Cannot export users");
        }
    }
}

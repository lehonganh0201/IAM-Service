package com.example.iamservice.service.importer;

import com.example.commonlib.exception.BadRequestException;
import com.example.iamservice.domain.dto.importer.ImportErrorItem;
import com.example.iamservice.domain.dto.importer.UserImportHeaders;
import com.example.iamservice.domain.dto.importer.UserImportRow;
import com.example.iamservice.domain.dto.response.ImportResultResponse;
import com.example.iamservice.domain.entity.User;
import com.example.iamservice.domain.entity.UserProfile;
import com.example.iamservice.repository.UserRepository;
import com.example.iamservice.validation.ImportRowValidator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    08/07/2026 at 11:24
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
public class ImportService {
    static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final UserRepository userRepository;
    private final List<ImportRowValidator> validators;

    public byte[] template() {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet s = wb.createSheet("Users");
            Row h = s.createRow(0);
            Font f = wb.createFont();
            f.setFontName("Times New Roman");
            f.setBold(true);
            CellStyle st = wb.createCellStyle();
            st.setFont(f);
            for (int i = 0; i < UserImportHeaders.REQUIRED.size(); i++) {
                Cell c = h.createCell(i);
                c.setCellValue(UserImportHeaders.REQUIRED.get(i));
                c.setCellStyle(st);
                s.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BadRequestException("Cannot create template");
        }
    }

    @Transactional
    public ImportResultResponse importUsers(MultipartFile file, boolean dryRun) {
        if (file == null || file.isEmpty()) throw new BadRequestException("File không được rỗng");
        if (file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase().endsWith(".xlsx"))
            throw new BadRequestException("Chỉ nhận file .xlsx");
        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sh = wb.getSheetAt(0);
            checkHeader(sh.getRow(0));
            List<ImportErrorItem> errs = new ArrayList<>();
            List<UserImportRow> rows = parse(sh, errs);
            dupFile(rows, errs);
            dupDb(rows, errs);
            rows.forEach(r -> validators.forEach(v -> v.validate(r, errs)));
            int er = (int) errs.stream().map(ImportErrorItem::rowIndex).distinct().count();
            if (!errs.isEmpty()) {
                if (!dryRun)
                    throw new BadRequestException("Import file has validation errors");
                return new ImportResultResponse(rows.size(), rows.size() - er, er, errs);
            }
            if (!dryRun) userRepository.saveAll(rows.stream().map(this::entity).toList());
            return new ImportResultResponse(rows.size(), rows.size(), 0, List.of());
        } catch (IOException e) {
            throw new BadRequestException("Không đọc được file Excel");
        }
    }

    void checkHeader(Row h) {
        if (h == null) throw new BadRequestException("File thiếu header");
        for (int i = 0; i < UserImportHeaders.REQUIRED.size(); i++)
            if (!UserImportHeaders.REQUIRED.get(i).equals(val(h.getCell(i))))
                throw new BadRequestException("Header không đúng tại cột " + (i + 1));
    }

    String val(Cell c) {
        return c == null ? "" : new DataFormatter(Locale.forLanguageTag("vi-VN")).formatCellValue(c).trim();
    }

    List<UserImportRow> parse(Sheet s, List<ImportErrorItem> e) {
        List<UserImportRow> r = new ArrayList<>();
        for (int i = 1; i <= s.getLastRowNum(); i++) {
            Row row = s.getRow(i);
            if (row == null) continue;
            r.add(new UserImportRow(i + 1, num(row.getCell(0), i + 1, "STT", e), val(row.getCell(1)), val(row.getCell(2)), date(row.getCell(3), i + 1, e), val(row.getCell(4)), val(row.getCell(5)), val(row.getCell(6)), val(row.getCell(7)), dbl(row.getCell(8), i + 1, "Số năm kinh nghiệm", e)));
        }
        return r;
    }

    void dupFile(List<UserImportRow> rows, List<ImportErrorItem> e) {
        Set<String> s = new HashSet<>();
        for (var r : rows)
            if (r.username() != null && !r.username().isBlank() && !s.add(r.username().toLowerCase()))
                e.add(new ImportErrorItem(r.excelRowIndex(), "username", r.username(), "username bị trùng trong file"));
    }

    void dupDb(List<UserImportRow> rows, List<ImportErrorItem> e) {
        Set<String> us = rows.stream().map(UserImportRow::username).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> ex = userRepository.findByUsernameIn(us).stream().map(User::getUsername).collect(Collectors.toSet());
        rows.stream().filter(r -> ex.contains(r.username())).forEach(r -> e.add(new ImportErrorItem(r.excelRowIndex(), "username", r.username(), "Username đã tồn tại")));
    }

    User entity(UserImportRow r) {
        UserProfile profile = new UserProfile();
        profile.setWard(r.ward());
        profile.setDistrict(r.district());
        profile.setProvince(r.province());
        profile.setStreet(r.street());
        profile.setYearsOfExperience(r.yearsOfExperience());

        User e = new User();
        e.setUsername(r.username());
        e.setFirstName(r.fullName());
        e.setDateOfBirth(r.dateOfBirth());
        return e;
    }

    Integer num(Cell c, int row, String f, List<ImportErrorItem> e) {
        try {
            return (int) c.getNumericCellValue();
        } catch (Exception x) {
            e.add(new ImportErrorItem(row, f, val(c), f + " phải là số"));
            return null;
        }
    }

    Double dbl(Cell c, int row, String f, List<ImportErrorItem> e) {
        try {
            return c.getNumericCellValue();
        } catch (Exception x) {
            e.add(new ImportErrorItem(row, f, val(c), f + " phải là number"));
            return null;
        }
    }

    LocalDate date(Cell c, int row, List<ImportErrorItem> e) {
        if (c == null || val(c).isBlank()) return null;
        try {
            if (c.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(c))
                return c.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return LocalDate.parse(val(c), DF);
        } catch (Exception x) {
            e.add(new ImportErrorItem(row, "Ngày sinh", val(c), "Ngày sinh không hợp lệ"));
            return null;
        }
    }
}

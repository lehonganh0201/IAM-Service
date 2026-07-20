package com.example.userservice.application.exporter;

import com.example.commonlib.exception.BadRequestException;
import com.example.userservice.infrastructure.persistence.UserEntity;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 17:09
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
public class XlsxUserExportStrategy implements UserExportStrategy {
    static final String[] H = CsvUserExportStrategy.H;
    static final DateTimeFormatter D = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String format() {
        return "xlsx";
    }

    public String contentType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    public String filename() {
        return "users.xlsx";
    }

    public byte[] export(List<UserEntity> users) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet s = wb.createSheet("Users");
            s.createFreezePane(0, 1);
            Font hf = wb.createFont();
            hf.setFontName("Times New Roman");
            hf.setBold(true);
            hf.setColor(IndexedColors.WHITE.getIndex());
            CellStyle hs = wb.createCellStyle();
            hs.setFont(hf);
            hs.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            hs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Row h = s.createRow(0);
            for (int i = 0; i < H.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(H[i]);
                c.setCellStyle(hs);
            }
            int r = 1;
            for (var u : users) {
                Row row = s.createRow(r);
                Object[] v = {r,
                        u.getUsername(),
                        u.getFullName(),
                        u.getDateOfBirth() == null ? "" : D.format(u.getDateOfBirth()),
                        u.getStreet(),
                        u.getWard(),
                        u.getDistrict(),
                        u.getProvince(),
                        u.getYearsOfExperience(),
                        u.getCreatedAt() == null ? "" : D.format(u.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate()),
                        u.getStatus().name()};
                for (int i = 0; i < v.length; i++)
                    row.createCell(i).setCellValue(v[i] == null ? "" : String.valueOf(v[i]));
                r++;
            }
            for (int i = 0; i < H.length; i++) s.autoSizeColumn(i);
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BadRequestException("Cannot export users");
        }
    }
}

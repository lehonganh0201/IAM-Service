package com.example.userservice.application.usecase;

import com.example.commonlib.exception.BadRequestException;
import com.example.userservice.application.importer.UserImportHeaders;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 16:40
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@RequiredArgsConstructor
public class UserImportUseCase {
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
}

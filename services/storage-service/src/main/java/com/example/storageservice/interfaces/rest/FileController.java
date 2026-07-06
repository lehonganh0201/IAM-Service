package com.example.storageservice.interfaces.rest;

import com.example.commonlib.api.RestApiV1;
import com.example.commonlib.api.common.ApiResponse;
import com.example.commonlib.api.common.ApiResponseFactory;
import com.example.commonlib.security.SecurityUtils;
import com.example.storageservice.application.dto.response.FileMetaDataResponse;
import com.example.storageservice.application.usecase.FileUseCases;
import com.example.storageservice.domain.model.FileVisibility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:49
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@RestApiV1
@RequiredArgsConstructor
public class FileController {
    private final FileUseCases useCases;
    private final ApiResponseFactory responseFactory;

    @PostMapping(value = "/public/files", consumes = "multipart/form-data")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FileMetaDataResponse>> upPub(@RequestPart MultipartFile file,
                                                                   @RequestParam(required = false) String description,
                                                                   @RequestParam(required = false) String tags) {
        return ResponseEntity.ok(
                responseFactory.success("Uploaded successfully", useCases.upload(
                        file,
                        FileVisibility.PUBLIC,
                        description,
                        tags,
                        SecurityUtils.currentUser())));
    }

    @PostMapping(value = "/api/v1/private/files", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyAuthority('storage:private:write','storage:file:write','storage:file:admin','ROLE_admin')")
    public ResponseEntity<ApiResponse<FileMetaDataResponse>> upPri(@RequestPart MultipartFile file,
                                                                   @RequestParam(required = false) String description,
                                                                   @RequestParam(required = false) String tags) {
        return ResponseEntity.ok(
                responseFactory.success("Uploaded successfully", useCases.upload(
                        file,
                        FileVisibility.PRIVATE,
                        description,
                        tags,
                        SecurityUtils.currentUser()))
        );
    }

    @PostMapping(value = "/api/v1/public/files/batch", consumes = "multipart/form-data")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<FileMetaDataResponse>>> batchPub(@RequestPart List<MultipartFile> files,
                                                                            @RequestParam(required = false) String description,
                                                                            @RequestParam(required = false) String tags) {
        return ResponseEntity.ok(
                responseFactory.success("Uploaded successfully",
                        files.stream().map(f ->
                                        useCases.upload(
                                                f,
                                                FileVisibility.PUBLIC,
                                                description,
                                                tags,
                                                SecurityUtils.currentUser()))
                                .toList()
                )
        );
    }

    @PostMapping(value = "/api/v1/private/files/batch", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyAuthority('storage:private:write','storage:file:write','storage:file:admin','ROLE_admin')")
    public ResponseEntity<ApiResponse<List<FileMetaDataResponse>>> batchPri(@RequestPart List<MultipartFile> files, @RequestParam(required = false) String description, @RequestParam(required = false) String tags) {
        return ResponseEntity.ok(
                responseFactory.success("Uploaded successfully",
                        files.stream().map(f ->
                                        useCases.upload(
                                                f,
                                                FileVisibility.PRIVATE,
                                                description,
                                                tags,
                                                SecurityUtils.currentUser()))
                                .toList()
                )
        );
    }
}

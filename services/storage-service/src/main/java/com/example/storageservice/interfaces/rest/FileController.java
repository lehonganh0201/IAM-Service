package com.example.storageservice.interfaces.rest;

import com.example.commonlib.api.RestApiV1;
import com.example.commonlib.api.common.ApiResponse;
import com.example.commonlib.api.common.ApiResponseFactory;
import com.example.commonlib.api.common.PageResponse;
import com.example.commonlib.api.common.PageableFactory;
import com.example.commonlib.security.SecurityUtils;
import com.example.storageservice.application.dto.request.FileSearchQuery;
import com.example.storageservice.application.dto.request.FileUpdateRequest;
import com.example.storageservice.application.dto.response.FileMetaDataResponse;
import com.example.storageservice.application.usecase.FileUseCases;
import com.example.storageservice.domain.model.FileVisibility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
    private final PageableFactory pageableFactory;

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

    @PostMapping(value = "/private/files", consumes = "multipart/form-data")
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

    @PostMapping(value = "/public/files/batch", consumes = "multipart/form-data")
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

    @PostMapping(value = "/private/files/batch", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyAuthority('storage:private:write','storage:file:write','storage:file:admin','ROLE_admin')")
    public ResponseEntity<ApiResponse<List<FileMetaDataResponse>>> batchPri(@RequestPart List<MultipartFile> files,
                                                                            @RequestParam(required = false) String description,
                                                                            @RequestParam(required = false) String tags) {
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

    @GetMapping({"/api/v1/public/files", "/api/v1/private/files"})
    public ResponseEntity<ApiResponse<PageResponse<FileMetaDataResponse>>> list(HttpServletRequest request,
                                                                                @RequestParam(required = false) String keyword,
                                                                                @RequestParam(required = false) String contentType,
                                                                                @RequestParam(required = false) String extension,
                                                                                @RequestParam(required = false) String ownerId,
                                                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
                                                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
                                                                                Pageable pageable) {
        FileVisibility v = request.getRequestURI().contains("/private/") ? FileVisibility.PRIVATE : FileVisibility.PUBLIC;

        return ResponseEntity.ok(responseFactory.success(
                "Search files successfully",
                useCases.search(
                        new FileSearchQuery(v, keyword, contentType, extension, ownerId, fromDate, toDate), pageable))
        );
    }

    @GetMapping({"/public/files/{id}/download", "/private/files/{id}/download"})
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        var r = useCases.download(id, SecurityUtils.currentUser());
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(r.contentType())).contentLength(r.contentLength()).header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(r.filename()).build().toString()).body(r.resource());
    }

    @GetMapping({"/api/v1/public/files/{id}", "/api/v1/private/files/{id}"})
    public ResponseEntity<ApiResponse<FileMetaDataResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(
                responseFactory.success(
                        "Get file metadata successfully",
                        useCases.get(id, SecurityUtils.currentUser())));
    }

    @GetMapping({"/api/v1/public/files/{id}/view", "/api/v1/private/files/{id}/view"})
    public ResponseEntity<Resource> view(@PathVariable UUID id) {
        var r = useCases.download(id, SecurityUtils.currentUser());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(r.contentType()))
                .body(r.resource());
    }

    @PatchMapping({"/api/v1/public/files/{id}", "/api/v1/private/files/{id}"})
    public ResponseEntity<ApiResponse<FileMetaDataResponse>> patch(@PathVariable UUID id, @Valid @RequestBody FileUpdateRequest req) {
        return ResponseEntity.ok(
                responseFactory.success("Updated successfully",
                        useCases.update(id, req, SecurityUtils.currentUser()))
        );
    }

    @PutMapping(value = {"/api/v1/public/files/{id}/content", "/api/v1/private/files/{id}/content"}, consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<FileMetaDataResponse>> replace(@PathVariable UUID id, @RequestPart MultipartFile file) {
        return ResponseEntity.ok(
                responseFactory.success("Content replaced successfully",
                        useCases.replaceContent(id, file, SecurityUtils.currentUser())
                )
        );
    }
}

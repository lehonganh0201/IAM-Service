package com.example.storageservice.domain.storage;

import com.example.storageservice.domain.model.StorageProvider;
import com.example.storageservice.domain.model.StorageResource;
import com.example.storageservice.domain.model.StoredObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:34
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public interface StorageStrategy {
    StorageProvider provider();

    StoredObject store(String originalFilename, String contentType, InputStream inputStream, long size) throws IOException;

    StorageResource load(String objectKey, String originalFilename, String contentType) throws IOException;

    void delete(String objectKey) throws IOException;
}

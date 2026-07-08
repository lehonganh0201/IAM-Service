package com.example.storageservice.domain.model;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:32
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record StoredObject(StorageProvider provider, String bucketName, String objectKey, String storedName) {
}

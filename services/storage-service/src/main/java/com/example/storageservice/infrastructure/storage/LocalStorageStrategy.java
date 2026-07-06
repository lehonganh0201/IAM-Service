package com.example.storageservice.infrastructure.storage;

import com.example.storageservice.application.service.FileNameFactory;
import com.example.storageservice.config.StorageProperties;
import com.example.storageservice.domain.model.StorageProvider;
import com.example.storageservice.domain.model.StorageResource;
import com.example.storageservice.domain.model.StoredObject;
import com.example.storageservice.domain.storage.StorageStrategy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:39
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
public class LocalStorageStrategy implements StorageStrategy {
    private final StorageProperties p;
    private final FileNameFactory f;

    public LocalStorageStrategy(StorageProperties p, FileNameFactory f) {
        this.p = p;
        this.f = f;
    }

    @Override
    public StorageProvider provider() {
        return StorageProvider.LOCAL;
    }

    @Override
    public StoredObject store(String n, String c, InputStream in, long size) throws IOException {
        String key = f.objectKey(n);
        Path t = root().resolve(key).normalize();
        if (!t.startsWith(root())) throw new IOException("Invalid path");
        Files.createDirectories(t.getParent());
        Files.copy(in, t, StandardCopyOption.REPLACE_EXISTING);
        return new StoredObject(StorageProvider.LOCAL, "local", key, t.getFileName().toString());
    }

    @Override
    public StorageResource load(String key, String name, String type) throws IOException {
        Path t = root().resolve(key).normalize();
        if (!t.startsWith(root()) || !Files.exists(t)) throw new IOException("File not found");
        return new StorageResource(new FileSystemResource(t), type, Files.size(t), name);
    }

    @Override
    public void delete(String key) throws IOException {
        Path t = root().resolve(key).normalize();
        if (t.startsWith(root())) Files.deleteIfExists(t);
    }

    private Path root() throws IOException {
        Path r = Path.of(p.localRoot()).toAbsolutePath().normalize();
        Files.createDirectories(r);
        return r;
    }
}

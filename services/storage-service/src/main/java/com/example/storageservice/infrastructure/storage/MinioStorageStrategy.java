package com.example.storageservice.infrastructure.storage;

import com.example.storageservice.application.service.FileNameFactory;
import com.example.storageservice.config.StorageProperties;
import com.example.storageservice.domain.model.StorageProvider;
import com.example.storageservice.domain.model.StorageResource;
import com.example.storageservice.domain.model.StoredObject;
import com.example.storageservice.domain.storage.StorageStrategy;
import io.minio.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:41
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
public class MinioStorageStrategy implements StorageStrategy {
    private final MinioClient c;
    private final StorageProperties p;
    private final FileNameFactory f;

    public MinioStorageStrategy(MinioClient c, StorageProperties p, FileNameFactory f) {
        this.c = c;
        this.p = p;
        this.f = f;
    }

    @Override
    public StorageProvider provider() {
        return StorageProvider.MINIO;
    }

    @Override
    public StoredObject store(String n, String type, InputStream in, long size) throws IOException {
        try {
            String b = p.minio().bucket();
            if (!c.bucketExists(BucketExistsArgs.builder().bucket(b).build()))
                c.makeBucket(MakeBucketArgs.builder().bucket(b).build());
            String key = f.objectKey(n);
            c.putObject(PutObjectArgs.builder().bucket(b).object(key).stream(in, size, -1).contentType(type).build());
            return new StoredObject(StorageProvider.MINIO, b, key, key.substring(key.lastIndexOf('/') + 1));
        } catch (Exception e) {
            throw new IOException("Cannot store to MinIO", e);
        }
    }

    @Override
    public StorageResource load(String key, String name, String type) throws IOException {
        try {
            var o = c.getObject(GetObjectArgs.builder().bucket(p.minio().bucket()).object(key).build());
            var st = c.statObject(StatObjectArgs.builder().bucket(p.minio().bucket()).object(key).build());
            return new StorageResource(new InputStreamResource(o), type, st.size(), name);
        } catch (Exception e) {
            throw new IOException("Cannot load from MinIO", e);
        }
    }

    @Override
    public void delete(String key) throws IOException {
        try {
            c.removeObject(RemoveObjectArgs.builder().bucket(p.minio().bucket()).object(key).build());
        } catch (Exception e) {
            throw new IOException("Cannot delete from MinIO", e);
        }
    }
}

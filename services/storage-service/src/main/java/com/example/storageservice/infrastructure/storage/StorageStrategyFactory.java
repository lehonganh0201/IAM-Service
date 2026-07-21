package com.example.storageservice.infrastructure.storage;

import com.example.storageservice.config.StorageProperties;
import com.example.storageservice.domain.model.StorageProvider;
import com.example.storageservice.domain.storage.StorageStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 11:43
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
public class StorageStrategyFactory {
    private final StorageProperties p;
    private final Map<StorageProvider, StorageStrategy> m = new EnumMap<>(StorageProvider.class);

    public StorageStrategyFactory(StorageProperties p, List<StorageStrategy> s) {
        this.p = p;
        s.forEach(x -> m.put(x.provider(), x));
    }

    public StorageStrategy current() {
        return m.get(p.provider());
    }

    public StorageStrategy byProvider(StorageProvider p) {
        return m.get(p);
    }
}

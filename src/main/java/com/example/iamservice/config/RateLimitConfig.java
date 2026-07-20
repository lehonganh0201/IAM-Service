package com.example.iamservice.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    10/06/2026 at 13:33
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Configuration
public class RateLimitConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient() {
        RedisURI redisURI = RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort)
                .build();
        return RedisClient.create(redisURI);
    }

    @Bean()
    public StatefulRedisConnection<String, byte[]> redisConnection(RedisClient redisClient) {
        return redisClient.connect(
                RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
        );
    }

    @Bean
    public ProxyManager<String> proxyManager(StatefulRedisConnection<String, byte[]> connection) {
        ClientSideConfig clientSideConfig = ClientSideConfig.getDefault();

        return LettuceBasedProxyManager.builderFor(connection)
                .withClientSideConfig(clientSideConfig)
                .build();
    }

    public BucketConfiguration createBucketConfig(int capacity, int refillAmount, long minutes) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(refillAmount, Duration.ofMinutes(minutes))
                .build();

        return BucketConfiguration.builder()
                .addLimit(limit)
                .build();
    }
}
package com.example.storageservice.application.service;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    06/07/2026 at 12:00
 * Project:       iam-platform
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
public class ChecksumService {
    public String sha256(byte[] b) {
        try {
            MessageDigest d = MessageDigest.getInstance("SHA-256");
            StringBuilder h = new StringBuilder();
            for (byte x : d.digest(b)) h.append(String.format("%02x", x));
            return h.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}


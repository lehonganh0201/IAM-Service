package com.example.iamservice.util;

import java.util.UUID;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    10/06/2026 at 10:59
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public class RandomUtil {

    public static String randomResetToken() {
        return UUID.randomUUID().toString();
    }
}

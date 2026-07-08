package com.example.iamservice.service;

import com.example.iamservice.constant.EmailTemplate;

import java.util.Map;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    10/06/2026 at 10:33
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */


public interface EmailService {
    void sendEmail(String to, EmailTemplate template, Map<String, Object> variables);
}

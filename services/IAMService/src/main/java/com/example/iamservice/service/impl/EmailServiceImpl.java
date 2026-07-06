package com.example.iamservice.service.impl;

import com.example.commonlib.exception.BadRequestException;
import com.example.iamservice.constant.EmailTemplate;
import com.example.iamservice.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    10/06/2026 at 10:33
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Service
@Log4j2
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    @Async
    public void sendEmail(String to, EmailTemplate template, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            if (variables != null) {
                variables.forEach(context::setVariable);
            }

            String htmlContent = templateEngine.process(template.getTemplatePath(), context);

            helper.setTo(to);
            helper.setSubject(template.getSubject());
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Email sent successfully to: {} | Template: {}", to, template.name());

        } catch (MessagingException e) {
            log.error("Failed to send email to: {} | Template: {}", to, template.name(), e);
            throw new BadRequestException("Không thể gửi email: " + e.getMessage());
        }
    }
}

package com.example.commonlib.web.context;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 16:10
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
@RequiredArgsConstructor
public class RequestContext {
    public static final String REQUEST_ID_KEY = "requestId";

    private final HttpServletRequest request;

    public String path() {
        return request.getRequestURI();
    }

    public String requestId() {
        String requestId = MDC.get(REQUEST_ID_KEY);

        if (requestId == null || requestId.isBlank()) {
            return request.getHeader("X-Request-Id");
        }

        return requestId;
    }
}

package com.example.iamservice.filter;

import com.example.iamservice.domain.entity.User;
import com.example.iamservice.repository.UserRepository;
import com.example.iamservice.security.IamPrincipal;
import com.example.iamservice.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.identity-provider.type",
        havingValue = "SELF",
        matchIfMissing = true
)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticateByToken(token);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateByToken(String token) {
        try {
            if (!jwtTokenProvider.validateAccessToken(token)) {
                return;
            }

            Long userId = jwtTokenProvider.getUserId(token);

            User user = userRepository.findById(userId)
                    .filter(u -> !Boolean.TRUE.equals(u.getDeleted()))
                    .orElse(null);

            if (user == null || !user.isActive()) {
                return;
            }

            IamPrincipal principal = new IamPrincipal(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    "SELF"
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            Collections.emptyList()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
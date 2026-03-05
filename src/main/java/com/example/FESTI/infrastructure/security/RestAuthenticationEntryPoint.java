package com.example.FESTI.infrastructure.security;

import com.example.FESTI.presentation.common.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Instant;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                HttpServletResponse.SC_UNAUTHORIZED,
                "UNAUTHORIZED",
                "Unauthorized",
                request.getRequestURI()
        );
        JSON_MAPPER.writeValue(response.getWriter(), body);
    }
}

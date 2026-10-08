package com.clayfin.training.minirib.security;

import com.clayfin.training.minirib.dto.response.ErrorResponse;
import com.clayfin.training.minirib.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorCode code = request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR) instanceof ErrorCode c
                ? c : ErrorCode.TOKEN_MISSING;

        ErrorResponse body = new ErrorResponse(LocalDateTime.now(), code.getHttpStatus().value(),
                code.name(), code.getMessage(), request.getRequestURI(), List.of());

        response.setStatus(code.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}

package com.kaua.events.platform.infrastructure.configurations.authentication;

import com.kaua.events.platform.domain.utils.InstantUtils;
import com.kaua.events.platform.infrastructure.configurations.json.Json;
import com.kaua.events.platform.infrastructure.utils.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");

        try {
            response.getWriter().write(Json.writeValueAsString(ApiError.from(
                    authException.getMessage(),
                    InstantUtils.now()
            )));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

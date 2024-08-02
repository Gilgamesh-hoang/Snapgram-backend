package org.snapgram.util;

import org.springframework.stereotype.Component;

@Component
public class EndPoint {

    public final String[] publicGetEndpoints() {
        return new String[]{
                "/api/v1/users/nickname-exists",
                "/api/v1/users/email-exists",
                "/swagger-ui/**",
                "/api-docs/**"
        };
    }

    public final String[] publicPostEndpoints() {
        return new String[]{
                "/api/v1/auth/login",
                "/api/v1/auth/verification-email",
                "/api/v1/auth/refresh-token",
                "/api/v1/users/forgot-password",
                "/api/v1/users",
        };
    }
}

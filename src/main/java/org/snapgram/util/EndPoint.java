package org.snapgram.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EndPoint {
    @Value("${API_PREFIX}")
    String apiPrefix;

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
                "/api/v1/users/forgot-password",
                "/api/v1/users",
        };
    }
}

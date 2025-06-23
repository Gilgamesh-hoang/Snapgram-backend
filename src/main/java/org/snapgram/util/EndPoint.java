package org.snapgram.util;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
public class EndPoint {

    public final String[] publicEndpoints() {
        return new String[]{
                "/api/v1/users/nickname-exists",
                "/api/v1/users/email-exists",
                "/swagger-ui/**",
                "/api-docs/**",
                "/swagger-ui.html",
                "/api/v1/auth/**",
                "/api/v1/users/forgot-password",
                "/api/v1/users/signup",
        };
    }

    public boolean isPublicPath(String requestPath) {
        for (String pattern : publicEndpoints()) {
            AntPathMatcher matcher = new AntPathMatcher();
            if (matcher.match(pattern, requestPath)) {
                return true;
            }
        }
        return false;
    }
}

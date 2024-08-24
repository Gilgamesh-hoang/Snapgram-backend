package org.snapgram.util;

import lombok.experimental.UtilityClass;
import org.snapgram.dto.CustomUserSecurity;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class UserSecurityHelper {

    public CustomUserSecurity getCurrentUser() {
        return (CustomUserSecurity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}

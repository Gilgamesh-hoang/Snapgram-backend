package org.snapgram.filter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.service.jwt.JwtHelper;
import org.snapgram.service.jwt.JwtService;
import org.snapgram.service.key.IKeyService;
import org.snapgram.service.user.UserDetailServiceImpl;
import org.snapgram.util.EndPoint;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*
The JwtRequestFilter extends the Spring Web Filter OncePerRequestFilter class. For any incoming request this Filter
class gets executed. It checks if the request has a valid JWT token. If it has a valid JWT Token then it sets the
 Authentication in the context, to specify that the current user is authenticated.
 */

@Component
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {
    IKeyService keyService;
    UserDetailServiceImpl userDetailService;
    JwtService jwtService;
    JwtHelper jwtHelper;
    EndPoint endPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestPath = request.getRequestURI();

        // check if the request path requires authentication
        boolean isPublic = endPoint.isPublicPath(requestPath);
        if (isPublic) {
            chain.doFilter(request, response); // continue the filter chain
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");
        String email = null;
        String jwtToken = null;
        // JWT Token is in the form "Bearer token". Remove Bearer word and get
        // only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring("Bearer ".length());
            try {
                email = jwtHelper.extractEmailFromPayload(jwtToken, false);
            } catch (IllegalArgumentException e) {
                log.warn("Unable to get JWT Token");
                throw new IllegalArgumentException("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                log.warn("JWT Token has expired");
            }
        } else {
            log.warn("JWT Token does not begin with Bearer String");
        }

        // Once we get the token validate it.
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            CustomUserSecurity userDetails = (CustomUserSecurity) userDetailService.loadUserByUsername(email);
            String publicKey = keyService.getUserPublicATKey(userDetails.getId());
            // if token is valid configure Spring Security to manually set
            // authentication
            if (jwtService.validateAccessToken(jwtToken, publicKey)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // After setting the Authentication in the context, we specify
                // that the current user is authenticated. So it passes the
                // Spring Security Configurations successfully.
                SecurityContextHolder.getContext().setAuthentication(authToken);

            }
        }
        chain.doFilter(request, response);
    }

}

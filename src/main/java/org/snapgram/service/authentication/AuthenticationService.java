package org.snapgram.service.authentication;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.request.AuthenticationRequest;
import org.snapgram.dto.request.LogoutRequest;
import org.snapgram.dto.response.JwtResponse;
import org.snapgram.jwt.JwtService;
import org.snapgram.service.token.ITokenService;
import org.snapgram.service.user.UserDetailServiceImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationService implements IAuthenticationService {
    final AuthenticationManager authenticationManager;
    final ITokenService tokenService;
    final JwtService jwtService;
    final UserDetailServiceImpl userDetailsService;

    @Override
    public JwtResponse login(AuthenticationRequest request) {
        UserDetails user = userDetailsService.loadUserByUsername(request.getEmail());

        // Authenticate the user using the provided email and password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        // If the user is authenticated
        if (authentication.isAuthenticated()) {
            // Generate a JWT token for the user
            String accessToken = jwtService.generateAccessToken(user.getUsername());
            String refreshToken = jwtService.generateRefreshToken(user.getUsername());

            // Return a JwtResponse with the generated token and its expiration time
            return new JwtResponse(accessToken, refreshToken);
        } else {
            throw new BadCredentialsException("Invalid email or password");
        }
    }


    @Override
    public void logout(LogoutRequest request) {
        tokenService.save(request.getToken());
        SecurityContextHolder.clearContext();
    }

}

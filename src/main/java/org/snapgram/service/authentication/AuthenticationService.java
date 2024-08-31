package org.snapgram.service.authentication;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.GooglePojo;
import org.snapgram.dto.request.AuthenticationRequest;
import org.snapgram.dto.response.JwtResponse;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.service.jwt.JwtHelper;
import org.snapgram.service.jwt.JwtService;
import org.snapgram.service.token.ITokenService;
import org.snapgram.service.user.IUserService;
import org.snapgram.service.user.UserDetailServiceImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService implements IAuthenticationService {
    AuthenticationManager authenticationManager;
    ITokenService tokenService;
    JwtService jwtService;
    JwtHelper jwtHelper;
    UserDetailServiceImpl userDetailsService;
    IUserService userService;

    @Override
    public JwtResponse oauth2GoogleLogin(GooglePojo googlePojo) {
        if (!googlePojo.isEmailVerified())
            throw new BadCredentialsException("Email is not verified");
        UserDTO user = userService.findByEmail(googlePojo.getEmail());
        if (user == null) {
            user = userService.createUser(googlePojo);
        }
        String accessToken = jwtService.generateAccessToken(googlePojo.getEmail());
        String refreshToken = jwtService.generateRefreshToken(googlePojo.getEmail());
        tokenService.saveRTInDB(refreshToken, user.getId());
        // Return a JwtResponse with the generated token and its expiration time
        return new JwtResponse(accessToken, refreshToken);
    }

    @Override
    public JwtResponse login(AuthenticationRequest request) {
        CustomUserSecurity user = (CustomUserSecurity) userDetailsService.loadUserByUsername(request.getEmail());

        // Authenticate the user using the provided email and password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        // If the user is authenticated
        if (authentication.isAuthenticated()) {
            // Generate a JWT token for the user
            String accessToken = jwtService.generateAccessToken(user.getUsername());
            String refreshToken = jwtService.generateRefreshToken(user.getUsername());

            tokenService.saveRTInDB(refreshToken, user.getId());

            // Return a JwtResponse with the generated token and its expiration time
            return new JwtResponse(accessToken, refreshToken);
        } else {
            throw new BadCredentialsException("Invalid email or password");
        }
    }


    @Override
    public void logout(String token, String refreshToken) {
        tokenService.saveAllInBlacklist(token, refreshToken);
        tokenService.deleteRefreshTokenInDB(refreshToken);
        SecurityContextHolder.clearContext();
    }

    @Override
    public JwtResponse refreshToken(String token) {
        // Validate the provided refresh token
        boolean isValid = jwtService.validateRefreshToken(token);
        if (!isValid) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        String email = jwtHelper.extractEmailFromRefreshToken(token);

        // Generate a new access token using the extracted email
        String accessToken = jwtService.generateAccessToken(email);
        String refreshToken = jwtService.generateRefreshToken(email);

        // Save the new refresh token in the database
        tokenService.saveRTInDB(token, refreshToken);

        // Add the old refresh token to the blacklist
        tokenService.saveRTInBlacklist(token);
        return new JwtResponse(accessToken, refreshToken);
    }


}

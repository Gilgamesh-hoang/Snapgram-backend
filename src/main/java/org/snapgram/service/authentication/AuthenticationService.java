package org.snapgram.service.authentication;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.GooglePojo;
import org.snapgram.dto.KeyPair;
import org.snapgram.dto.request.AuthenticationRequest;
import org.snapgram.dto.response.JwtResponse;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.kafka.producer.RedisProducer;
import org.snapgram.service.jwt.JwtHelper;
import org.snapgram.service.jwt.JwtService;
import org.snapgram.service.key.IKeyService;
import org.snapgram.service.token.ITokenService;
import org.snapgram.service.user.IUserService;
import org.snapgram.service.user.UserDetailServiceImpl;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
    IKeyService keyService;
    RedisProducer redisProducer;

    // Method to handle common logic for generating JWT response
    private JwtResponse generateJwtResponse(String email, KeyPair keyPair) throws InterruptedException, ExecutionException {
        CompletableFuture<String> accessToken = jwtService.generateAccessToken(email, keyPair.getPrivateKeyAT());
        CompletableFuture<String> refreshToken = jwtService.generateRefreshToken(email, keyPair.getPrivateKeyRT());
        CompletableFuture.allOf(accessToken, refreshToken).join();
        return new JwtResponse(accessToken.get(), refreshToken.get());
    }

    @Override
    public JwtResponse oauth2GoogleLogin(GooglePojo googlePojo) {
        if (!googlePojo.isEmailVerified())
            throw new BadCredentialsException("Email is not verified");
        UserDTO user = userService.findByEmail(googlePojo.getEmail());
        if (user == null) {
            user = userService.createUser(googlePojo);
        }
        return createResponse(user.getEmail(), user.getId());
    }

    private JwtResponse createResponse(String email, UUID userId) {
        KeyPair keyPair = keyService.getKeyPairByUser(userId);
        try {
            JwtResponse jwtResponse = generateJwtResponse(email, keyPair);
            tokenService.storeRefreshToken(jwtResponse.getRefreshToken(), userId);
            return jwtResponse;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BadCredentialsException("Error while generating token");
        }
    }

    @Override
    public JwtResponse login(AuthenticationRequest request) {
        CustomUserSecurity user = (CustomUserSecurity) userDetailsService.loadUserByUsername(request.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        if (authentication.isAuthenticated()) {
            return createResponse(request.getEmail(), user.getId());
        } else {
            throw new BadCredentialsException("Invalid email or password");
        }
    }


    @Override
    public void logout(String token, String refreshToken) {
        tokenService.blacklistTokens(token, refreshToken);
        tokenService.removeRefreshToken(refreshToken);
        SecurityContextHolder.clearContext();
    }

    @Override
    public JwtResponse refreshToken(String token) {
        String email = jwtHelper.getEmailFromRefreshToken(token);
        if (email == null) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        CustomUserSecurity user = (CustomUserSecurity) userDetailsService.loadUserByUsername(email);
        KeyPair keyPair = keyService.getKeyPairByUser(user.getId());
        boolean isValid = jwtService.validateRefreshToken(token, keyPair.getPublicKeyRT());
        if (!isValid) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        try {
            JwtResponse jwtResponse = generateJwtResponse(user.getEmail(), keyPair);
            tokenService.storeRefreshToken(token, jwtResponse.getRefreshToken());
            tokenService.blacklistRefreshToken(token);

            // Save last refresh token used
            redisProducer.sendSaveMap(RedisKeyUtil.LAST_REFRESH_TOKEN, Map.of(user.getId(), new Timestamp(System.currentTimeMillis())));
            return jwtResponse;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new BadCredentialsException("Error while generating token");
        }
    }

}

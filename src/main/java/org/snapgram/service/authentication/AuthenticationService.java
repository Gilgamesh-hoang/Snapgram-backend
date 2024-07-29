package org.snapgram.service.authentication;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.request.AuthenticationRequest;
import org.snapgram.dto.request.LogoutRequest;
import org.snapgram.dto.response.JwtResponse;
import org.snapgram.entity.Token;
import org.snapgram.entity.User;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.jwt.JwtTokenUtil;
import org.snapgram.repository.ITokenRepository;
import org.snapgram.repository.IUserRepository;
import org.snapgram.service.user.UserDetailServiceImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService implements IAuthenticationService {
    AuthenticationManager authenticationManager;
    IUserRepository userRepository;
    ITokenRepository tokenRepository;
    JwtTokenUtil jwtTokenUtil;
    UserDetailServiceImpl userDetailsService;

    @Override
    public JwtResponse login(AuthenticationRequest request) {
        UserDetails user = userDetailsService.loadUserByUsername(request.getEmail());

        // Authenticate the user using the provided email and password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        // If the user is authenticated
        if (authentication.isAuthenticated()) {
            // Generate a JWT token for the user
            String token = jwtTokenUtil.generateToken(user);

            // Extract the expiration date of the token
            Date expired = jwtTokenUtil.extractExpiration(token);

            // Load the user entity from the repository using the provided email
            User userEntity = userRepository.findByEmail(request.getEmail()).orElseThrow();

            // Build a token entity with the generated token, its expiration date, and the user entity
            Token tokenEntity = Token.builder()
                    .expiredDate(new Timestamp(expired.getTime()))
                    .token(token)
                    .user(userEntity)
                    .build();

            // Save the token entity in the repository
            tokenRepository.save(tokenEntity);

            // Return a JwtResponse with the generated token and its expiration time
            return new JwtResponse(token, System.currentTimeMillis() + JwtTokenUtil.ACCESS_TOKEN_LIFETIME);
        } else {
            throw new BadCredentialsException("Invalid email or password");
        }
    }


    @Override
    public void logout(LogoutRequest request) {
        Token token = tokenRepository.findByToken(request.getToken()).orElse(null);
        if (token == null)
            throw new ResourceNotFoundException("Token not found");

        token.setIsExpired(true);
        tokenRepository.save(token);
        SecurityContextHolder.clearContext();
    }

}

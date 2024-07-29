package org.snapgram.service.authentication;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.request.AuthenticationRequest;
import org.snapgram.dto.response.JwtResponse;
import org.snapgram.entity.Token;
import org.snapgram.entity.User;
import org.snapgram.jwt.JwtTokenUtil;
import org.snapgram.repository.ITokenRepository;
import org.snapgram.repository.IUserRepository;
import org.snapgram.service.user.UserDetailServiceImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    ITokenRepository invalidatedTokenRepository;
    JwtTokenUtil jwtTokenUtil;
    UserDetailServiceImpl userDetailsService;

    @Override
    public JwtResponse login(AuthenticationRequest request) {
        UserDetails user = userDetailsService.loadUserByUsername(request.getEmail());

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        boolean isAuthenticated = authentication.isAuthenticated();
        if (isAuthenticated) {
            String token = jwtTokenUtil.generateToken(user);
            Date expired = jwtTokenUtil.extractExpiration(token);
            User userEntity = userRepository.findByEmail(request.getEmail()).orElseThrow();
            Token tokenEntity = Token.builder()
                    .expiredDate(new Timestamp(expired.getTime()))
                    .token(token)
                    .user(userEntity)
                    .build();

            invalidatedTokenRepository.save(tokenEntity);
            return new JwtResponse(token, System.currentTimeMillis() + JwtTokenUtil.JWT_TOKEN_VALIDITY);
        } else {
            return null;
        }
    }


//    @Override
//    public void logout(LogoutRequest request) {
//
//        extractJwtIDAndSaveToken(request.getToken());
//        SecurityContextHolder.clearContext();
//    }

//    private void extractJwtAndSaveToken(String token) {
//        Date expired = jwtTokenUtil.extractExpiration(token);
//
//        Token invalidatedToken = Token.builder()
//                .expiredDate(new Timestamp(expired.getTime()))
//                .token(token)
//                .build();
//        invalidatedTokenRepository.save(invalidatedToken);
//    }

}

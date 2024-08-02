package org.snapgram.service.authentication;


import org.snapgram.dto.request.AuthenticationRequest;
import org.snapgram.dto.request.LogoutRequest;
import org.snapgram.dto.request.TokenRequest;
import org.snapgram.dto.response.JwtResponse;

public interface IAuthenticationService {
    JwtResponse login(AuthenticationRequest request);

    void logout(LogoutRequest request);

    JwtResponse refreshToken(String token);
}

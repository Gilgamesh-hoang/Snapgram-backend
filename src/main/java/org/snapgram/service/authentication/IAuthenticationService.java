package org.snapgram.service.authentication;


import org.snapgram.dto.request.AuthenticationRequest;
import org.snapgram.dto.response.JwtResponse;

public interface IAuthenticationService {
    JwtResponse login(AuthenticationRequest request);

    void logout(String token,String refreshToken);

    JwtResponse refreshToken(String token);
}

package org.snapgram.service.token;

import org.snapgram.dto.response.TokenDTO;

public interface ITokenService {

    void delete(String token);

    void saveAll(String accessToken,String refreshToken);

    TokenDTO findToken(String token);
}

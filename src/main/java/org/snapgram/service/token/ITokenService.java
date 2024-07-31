package org.snapgram.service.token;

import org.snapgram.dto.response.TokenDTO;

public interface ITokenService {

    void delete(String token);

    void save(String token);

    TokenDTO findToken(String token);
}

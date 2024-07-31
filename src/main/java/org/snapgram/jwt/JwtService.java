package org.snapgram.jwt;


import com.fasterxml.uuid.Generators;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.TokenDTO;
import org.snapgram.service.token.ITokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
The JwtService is responsible for performing JWT operations like creation and validation.
It makes use of the io.jsonwebtoken.Jwt for achieving this.
 */

@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class JwtService {
    final ITokenService tokenService;
    final JwtHelper jwtHelper;
    @Value("${jwt.access_token.duration}")
    long ACCESS_TOKEN_LIFETIME;
    @Value("${jwt.access_token.secret_key}")
    String ACCESS_TOKEN_KEY;
    @Value("${jwt.refresh_token.duration}")
    long REFRESH_TOKEN_LIFETIME;
    @Value("${jwt.refresh_token.secret_key}")
    String REFRESH_TOKEN_KEY;

//    @Autowired
//    public void setTokenService(ITokenService tokenService) {
//        this.tokenService = tokenService;
//    }

    public String generateAccessToken(String email) {
        if (email == null)
            return null;

        Map<String, Object> claims = new HashMap<>();
        claims.put("jid", Generators.randomBasedGenerator().generate().toString());
        return createToken(claims, email, false);
    }

    public String generateRefreshToken(String email) {
        if (email == null)
            return null;
        return createToken(new HashMap<>(), email, true);
    }


    // While creating the token -
    // 1. Define claims of the token, like Issuer, Expiration, Subject, and the ID
    // 2. Sign the JWT using the HS512 algorithm and secret key.
    // 3. According to JWS Compact
    // Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    // compaction of the JWT to a URL-safe string
    private String createToken(Map<String, Object> claims, String email, boolean isRefreshToken) {
        JwtBuilder jwt = Jwts.builder().setHeaderParam("type", "JWT")
                .setClaims(claims).setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()));

        if (isRefreshToken) {
            jwt.setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_LIFETIME))
                    .signWith(jwtHelper.getSigningKey(REFRESH_TOKEN_KEY), SignatureAlgorithm.HS256);
        } else {
            jwt.setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_LIFETIME))
                    .signWith(jwtHelper.getSigningKey(ACCESS_TOKEN_KEY), SignatureAlgorithm.HS256);
        }
        return jwt.compact();
    }


    /**
     * This method is used to check if a provided JWT token is expired.
     * The token is considered expired if its expiration date is before the current time.
     *
     * @param token The JWT token to check for expiration.
     * @return true if the token is expired, false otherwise.
     */
    private boolean isTokenExpired(String token) {
        return jwtHelper.extractExpiration(token).before(new Timestamp(System.currentTimeMillis()));
    }


    private boolean isExistsInBlacklist(String token) {
        TokenDTO t = tokenService.findToken(token);
        return t != null;
    }


    public boolean validateToken(String token, UserDetails userDetails) {
        final String email = jwtHelper.extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token) && !isExistsInBlacklist(token);
    }

}

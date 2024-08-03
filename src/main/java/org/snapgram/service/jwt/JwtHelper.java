package org.snapgram.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtHelper {

    @Value("${jwt.access_token.secret_key}")
    private String ACCESS_TOKEN_KEY;
    @Value("${jwt.refresh_token.secret_key}")
    String REFRESH_TOKEN_KEY;

    /**
     * This method is used to get the signing key from a provided string key.
     * The string key is first decoded from BASE64 and then used to generate a HMAC SHA key.
     *
     * @param key The string key to be used for generating the signing key.
     * @return The generated signing key.
     */
    public Key getSigningKey(String key) {
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * This method is used to extract all claims from a provided JWT token.
     * The token is parsed using the signing key.
     *
     * @param token The JWT token to extract claims from.
     * @return The extracted claims.
     */
    private Claims extractAllClaims(String token, Key key) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    /**
     * This method is used to extract a specific claim from a provided JWT token.
     * The specific claim to extract is determined by the provided Function.
     *
     * @param token          The JWT token to extract a claim from.
     * @param claimsFunction The Function to determine which claim to extract.
     * @return The extracted claim.
     */
    private <T> T extractClaims(String token, Function<Claims, T> claimsFunction, Key key) {
        final Claims claims = extractAllClaims(token, key);
        return claimsFunction.apply(claims);
    }

    public String extractEmailFromToken(String token) {
        return extractClaims(token, Claims::getSubject, getSigningKey(ACCESS_TOKEN_KEY));
    }

    public Date extractExpirationFromToken(String token) {
        return extractClaims(token, Claims::getExpiration, getSigningKey(ACCESS_TOKEN_KEY));
    }

    public String getJidFromToken(String token) {
        return extractClaims(token, claims -> claims.get("jid", String.class), getSigningKey(ACCESS_TOKEN_KEY));
    }
    public String getJidFromRefreshToken(String token) {
        return extractClaims(token, claims -> claims.get("jid", String.class), getSigningKey(REFRESH_TOKEN_KEY));
    }

    public String extractEmailFromRefreshToken(String token) {
        return extractClaims(token, Claims::getSubject, getSigningKey(REFRESH_TOKEN_KEY));
    }

    public Date extractExpirationFromRefreshToken(String token) {
        return extractClaims(token, Claims::getExpiration, getSigningKey(REFRESH_TOKEN_KEY));
    }

}

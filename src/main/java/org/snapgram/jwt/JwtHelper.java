package org.snapgram.jwt;

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

    public String getJidFromToken(String token) {
        return extractClaims(token, claims -> claims.get("jid", String.class));
    }

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
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey(ACCESS_TOKEN_KEY)).build().parseClaimsJws(token).getBody();
    }

    /**
     * This method is used to extract a specific claim from a provided JWT token.
     * The specific claim to extract is determined by the provided Function.
     *
     * @param token          The JWT token to extract a claim from.
     * @param claimsFunction The Function to determine which claim to extract.
     * @return The extracted claim.
     */
    private <T> T extractClaims(String token, Function<Claims, T> claimsFunction) {
        final Claims claims = extractAllClaims(token);
        return claimsFunction.apply(claims);
    }

    /**
     * This method is used to extract the email (subject) from a provided JWT token.
     *
     * @param email The JWT token to extract the email from.
     * @return The extracted email.
     */
    public String extractEmail(String email) {
        return extractClaims(email, Claims::getSubject);
    }

    /**
     * This method is used to extract the expiration date from a provided JWT token.
     *
     * @param token The JWT token to extract the expiration date from.
     * @return The extracted expiration date.
     */
    public Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }


}

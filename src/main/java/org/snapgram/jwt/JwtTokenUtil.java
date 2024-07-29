package org.snapgram.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.repository.ITokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.security.Key;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/*
The JwtTokenUtil is responsible for performing JWT operations like creation and validation.
It makes use of the io.jsonwebtoken.Jwt for achieving this.
 */

@Component
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class JwtTokenUtil implements Serializable {
    final ITokenRepository tokenRepository;
    public static final long JWT_TOKEN_VALIDITY = (long) 5 * 60 * 60 * 1000;
    @Value("${jwt.secret-key}")
    String SECRET_KEY;


    public String generateToken(UserDetails userDetails) {
        if (userDetails == null)
            return null;

        Map<String, Object> claims = putClaims();
        return createToken(claims, userDetails.getUsername());
    }

    private Map<String, Object> putClaims() {
        return new HashMap<>();
    }

    // While creating the token -
    // 1. Define claims of the token, like Issuer, Expiration, Subject, and the ID
    // 2. Sign the JWT using the HS512 algorithm and secret key.
    // 3. According to JWS Compact
    // Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    // compaction of the JWT to a URL-safe string
    private String createToken(Map<String, Object> claims, String email) {
        return Jwts.builder().setHeaderParam("type", "JWT")
                .setClaims(claims).setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(getSigningKey(SECRET_KEY), SignatureAlgorithm.HS256).compact();
    }

    /**
     * This method is used to get the signing key from a provided string key.
     * The string key is first decoded from BASE64 and then used to generate a HMAC SHA key.
     *
     * @param key The string key to be used for generating the signing key.
     * @return The generated signing key.
     */
    private Key getSigningKey(String key) {
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
        return Jwts.parserBuilder().setSigningKey(getSigningKey(SECRET_KEY)).build().parseClaimsJws(token).getBody();
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

    /**
     * This method is used to check if a provided JWT token is expired.
     * The token is considered expired if its expiration date is before the current time.
     *
     * @param token The JWT token to check for expiration.
     * @return true if the token is expired, false otherwise.
     */
    private boolean isTokenExpired(String token) {
        return !extractExpiration(token).before(new Timestamp(System.currentTimeMillis()));
    }

    /**
     * This method is used to check if a provided JWT token exists in the token repository.
     *
     * @param token The JWT token to check for existence.
     * @return true if the token exists in the repository, false otherwise.
     */
    private boolean isExists(String token) {
        return tokenRepository.existsById(token);
    }

    /**
     * This method is used to validate a provided JWT token.
     * The token is considered valid if the username extracted from the token matches the username of the provided UserDetails,
     * the token is not expired, and the token does not exist in the token repository.
     *
     * @param token       The JWT token to validate.
     * @param userDetails The UserDetails to compare the username with.
     * @return true if the token is valid, false otherwise.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        return username.equals(userDetails.getUsername()) && isTokenExpired(token) && !isExists(token);
    }


//    public static void main(String[] args) {
//        long a = System.currentTimeMillis() + JwtTokenUtil.JWT_TOKEN_VALIDITY;
//        System.out.println(a);
//        Timestamp timestamp = new Timestamp(a);
//        System.out.println(timestamp);
//        System.out.println(timestamp.getTime());
//    }
}

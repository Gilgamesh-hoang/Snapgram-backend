package org.snapgram.service.jwt;


import com.fasterxml.uuid.Generators;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.TokenDTO;
import org.snapgram.service.token.ITokenService;
import org.snapgram.service.user.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class JwtService {
    final ITokenService tokenService;
    final JwtHelper jwtHelper;
    final UserDetailServiceImpl userDetailService;
    @Value("${jwt.access_token.duration}")
    long ACCESS_TOKEN_LIFETIME;
    @Value("${jwt.refresh_token.duration}")
    long REFRESH_TOKEN_LIFETIME;

    @Async
    public CompletableFuture<String> generateAccessToken(String email, String privateKey) {
        return CompletableFuture.supplyAsync(() -> {
            if (email == null)
                return null;
            Map<String, Object> claims = new HashMap<>();
            claims.put("jid", Generators.randomBasedGenerator().generate().toString());
            return createToken(claims, email, privateKey, ACCESS_TOKEN_LIFETIME);
        });
    }

    @Async
    public CompletableFuture<String> generateRefreshToken(String email, String privateKey) {
        return CompletableFuture.supplyAsync(() -> {
            if (email == null)
                return null;
            Map<String, Object> claims = new HashMap<>();
            claims.put("jid", Generators.randomBasedGenerator().generate().toString());
            return createToken(claims, email, privateKey, REFRESH_TOKEN_LIFETIME);

        });
    }

    private String createToken(Map<String, Object> claims, String email, String privateKey, long lifetime) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
            PrivateKey key = keyFactory.generatePrivate(privateKeySpec);
            JwtBuilder jwt = Jwts.builder().setHeaderParam("type", "JWT")
                    .setClaims(claims)
                    .setSubject(email)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + lifetime))
                    .signWith(key, SignatureAlgorithm.RS256);
            return jwt.compact();
        } catch (Exception e) {
            throw new RuntimeException("Error while creating token", e);
        }
    }

    private PublicKey getPublicKeyFromBase64String(String publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
            return keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error while generating public key", e);
        }
    }

    public boolean validateAccessToken(String token, String publicKey) {
        try {
            PublicKey key = getPublicKeyFromBase64String(publicKey);
            final String emailEx = jwtHelper.getEmailFromToken(token, key);
            userDetailService.loadUserByUsername(emailEx);
            String jid = jwtHelper.getJidFromAccessToken(token, key);
            return !isAccessTokenExpired(token, key) && !isExistsInBlacklist(jid);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token, String publicKey) {
        try {
            PublicKey key = getPublicKeyFromBase64String(publicKey);
            final String emailEx = jwtHelper.getEmailFromToken(token, key);
            userDetailService.loadUserByUsername(emailEx);
            String jid = jwtHelper.getJidFromRefreshToken(token, key);
            return !isRefreshTokenExpired(token, key) && !isExistsInBlacklist(jid);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isAccessTokenExpired(String token, PublicKey key) {
        return jwtHelper.getExpiryFromAccessToken(token, key).before(new Timestamp(System.currentTimeMillis()));
    }
    private boolean isRefreshTokenExpired(String token, PublicKey key) {
        return jwtHelper.getExpiryFromRefreshToken(token, key).before(new Timestamp(System.currentTimeMillis()));
    }


    private boolean isExistsInBlacklist(String jid) {
        TokenDTO t = tokenService.getTokenFromBlacklist(jid);
        return t != null;
    }
}

package org.snapgram.service.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.exception.ResourceNotFoundException;
import org.snapgram.service.key.IKeyService;
import org.snapgram.service.user.UserDetailServiceImpl;
import org.snapgram.util.UserSecurityHelper;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class JwtHelper {
    ObjectMapper objectMapper;
    IKeyService keyService;
    UserDetailServiceImpl detailService;

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

    public String extractEmailFromPayload(String token, boolean isRefreshToken) {
        String payload = decodeJwtPayload(token);
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            String email = jsonNode.get("sub").asText();
            if (email == null) {
                throw new ResourceNotFoundException("Email not found in token");
            }
            if (isRefreshToken) {
                return getEmailFromToken(token, getRefreshTokenPublicKeyByUser(email));
            } else
                return getEmailFromToken(token, getAccessTokenPublicKeyByUser(email));
        } catch (Exception e) {
            throw new ResourceNotFoundException("Failed to parse payload");
        }
    }

    public String getEmailFromToken(String token, Key key) {
        return extractClaims(token, Claims::getSubject, key);
    }

    public Date getExpiryFromAccessToken(String jwt) {
        String email = extractEmailFromPayload(jwt, false);
        return getExpiryFromAccessToken(jwt, getAccessTokenPublicKeyByUser(email));
    }

    public Date getExpiryFromAccessToken(String token, PublicKey key) {
        return extractClaims(token, Claims::getExpiration, key);
    }
    public Date getExpiryFromRefreshToken(String token, PublicKey key) {
        return extractClaims(token, Claims::getExpiration, key);
    }

    private PublicKey getUserPublicKey(String email, Function<UUID, String> getPublicKeyFunction) {
        CustomUserSecurity userDetails;
        try {
            userDetails = UserSecurityHelper.getCurrentUser();
        } catch (Exception e) {
            userDetails = (CustomUserSecurity) detailService.loadUserByUsername(email);
        }

        try {
            String publicKey = getPublicKeyFunction.apply(userDetails.getId());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
            return keyFactory.generatePublic(publicKeySpec);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Failed to get public key");
        }
    }

    private PublicKey getAccessTokenPublicKeyByUser(String email) {
        return getUserPublicKey(email, keyService::getUserPublicATKey);
    }

    private PublicKey getRefreshTokenPublicKeyByUser(String email) {
        return getUserPublicKey(email, keyService::getUserPublicRTKey);
    }

    private String decodeJwtPayload(String jwt) {
        String[] parts = jwt.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token format.");
        }

        String payload = parts[1];
        byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
        return new String(decodedBytes);
    }

    public String getJidFromAccessToken(String token, Key key) {
        return extractClaims(token, claims -> claims.get("jid", String.class), key);
    }

    public String getJidFromAccessToken(String token) {
        String email = extractEmailFromPayload(token, false);
        return getJidFromAccessToken(token, getAccessTokenPublicKeyByUser(email));
    }

    public String getJidFromRefreshToken(String token) {
        String email = extractEmailFromPayload(token, true);
        return getJidFromRefreshToken(token, getRefreshTokenPublicKeyByUser(email));
    }

    public String getJidFromRefreshToken(String token, Key key) {
        return extractClaims(token, claims -> claims.get("jid", String.class), key);
    }

    public String getEmailFromRefreshToken(String token) {
        String email = extractEmailFromPayload(token, true);
        return getEmailFromRefreshToken(token, getRefreshTokenPublicKeyByUser(email));
    }

    public String getEmailFromRefreshToken(String token, Key key) {
        return extractClaims(token, Claims::getSubject, key);
    }

    public Date getRefreshTokenExpiry(String token) {
        String email = extractEmailFromPayload(token, true);
        return extractClaims(token, Claims::getExpiration, getRefreshTokenPublicKeyByUser(email));
    }

}

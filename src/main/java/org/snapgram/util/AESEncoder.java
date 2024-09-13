package org.snapgram.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class AESEncoder {
    @Value("${aes.secret_key}")
    private String SECRET_KEY;

    // Method to encrypt a string using AES/GCM/NoPadding
    @Async
    public CompletableFuture<String> encode(String data) {
        try {
            // Convert the key string to bytes
            byte[] keyBytes = SECRET_KEY.getBytes();
            // Create a SecretKeySpec object from the key bytes
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            // Get a Cipher instance for the AES/GCM/NoPadding algorithm
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            // Generate a new random initialization vector
            byte[] iv = new byte[8];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(128, iv); // GCM parameter spec
            // Initialize the cipher for encryption using the secret key and the generated initialization vector
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            // Encrypt the data bytes using the cipher
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            // Prepend the IV to the encrypted data
            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);
            // Encode the combined byte array (IV + encrypted data) to a string using Base64 and return it
            String result = Base64.getUrlEncoder().withoutPadding().encodeToString(combined);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            // Print the stack trace for any exceptions
            log.error("Error while encrypting data", e);
        }
        // Return null if an exception occurred
        return CompletableFuture.completedFuture(null);
    }

    // Method to decrypt a string using AES/GCM/NoPadding
    @Async
    public CompletableFuture<String> decode(String encryptedData) {
        try {
            // Convert the key string to bytes
            byte[] keyBytes = SECRET_KEY.getBytes();
            // Create a SecretKeySpec object from the key bytes
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            // Decode the encrypted data from Base64 to bytes
            byte[] combined = Base64.getUrlDecoder().decode(encryptedData);
            // Extract the IV from the beginning of the combined byte array
            byte[] iv = Arrays.copyOfRange(combined, 0, 8);
            // Extract the encrypted data from the combined byte array
            byte[] encryptedBytes = Arrays.copyOfRange(combined, 8, combined.length);
            // Get a Cipher instance for the AES/GCM/NoPadding algorithm
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            // Initialize the cipher for decryption using the secret key and the extracted IV
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));
            // Decrypt the encrypted bytes using the cipher
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            // Convert the decrypted bytes to a string and return it
            String result = new String(decryptedBytes);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            // Print the stack trace for any exceptions
            log.error("Error while decrypting data", e);
        }
        // Return null if an exception occurred
        return CompletableFuture.completedFuture(null);
    }
}

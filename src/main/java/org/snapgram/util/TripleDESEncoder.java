package org.snapgram.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.util.Base64;

@Component
@Slf4j
public class TripleDESEncoder {
    @Value("${triple_des.secret_key}")
    private String SECRET_KEY;

    // Method to encrypt a string using Triple DES
    public String encode(String data) {
        try {
            // Convert the key string to bytes
            byte[] keyBytes = SECRET_KEY.getBytes();
            // Create a DESedeKeySpec object from the key bytes
            DESedeKeySpec keySpec = new DESedeKeySpec(keyBytes);
            // Get a SecretKeyFactory instance for the Triple DES algorithm
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
            // Generate a SecretKey from the keySpec
            SecretKey secretKey = keyFactory.generateSecret(keySpec);
            // Get a Cipher instance for the Triple DES algorithm
            Cipher cipher = Cipher.getInstance("DESede");
            // Initialize the cipher for encryption using the secret key
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            // Encrypt the data bytes using the cipher
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            // Encode the encrypted bytes to a string using Base64 and return it
            return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedBytes);
        } catch (Exception e) {
            // Print the stack trace for any exceptions
            log.error("Error while encrypting data", e);
        }
        // Return null if an exception occurred
        return null;
    }

    // Method to decrypt a string using Triple DES
    public String decode(String encryptedData) {
        try {
            // Convert the key string to bytes
            byte[] keyBytes = SECRET_KEY.getBytes();
            // Create a DESedeKeySpec object from the key bytes
            DESedeKeySpec keySpec = new DESedeKeySpec(keyBytes);
            // Get a SecretKeyFactory instance for the Triple DES algorithm
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
            // Generate a SecretKey from the keySpec
            SecretKey secretKey = keyFactory.generateSecret(keySpec);
            // Get a Cipher instance for the Triple DES algorithm
            Cipher cipher = Cipher.getInstance("DESede");
            // Initialize the cipher for decryption using the secret key
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            // Decode the encrypted data from Base64 to bytes
            byte[] decryptedBytes = cipher.doFinal(Base64.getUrlDecoder().decode(encryptedData));
            // Convert the decrypted bytes to a string and return it
            return new String(decryptedBytes);
        } catch (Exception e) {
            // Print the stack trace for any exceptions
            log.error("Error while encrypting data", e);
        }
        // Return null if an exception occurred
        return null;
    }

}

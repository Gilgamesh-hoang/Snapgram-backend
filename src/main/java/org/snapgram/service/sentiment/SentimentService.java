package org.snapgram.service.sentiment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.snapgram.enums.SentimentType;
import org.snapgram.service.jwt.JwtService;
import org.snapgram.service.redis.IRedisService;
import org.snapgram.util.AESEncoder;
import org.snapgram.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@DependsOn("dotenv")
public class SentimentService implements ISentimentService {
    final OkHttpClient client;
    final ObjectMapper objectMapper;
    final IRedisService redisService;
    final JwtService jwtService;
    final AESEncoder encoder;

    @Value("${application.sentiment-service.url}")
    String sentimentServiceUrl;
    @Value("${JWT_PRIVATE_KEY}")
    String privateKey;
    @Value("${jwt.access_token.duration}")
    long ACCESS_TOKEN_LIFETIME;

    //    @PostConstruct
//    public void init() {
//        System.out.println("JWT_PRIVATE_KEY in SentimentService: " + privateKey);
//    }

    @Override
    public SentimentType analyzeSentiment(@NotBlank String text) {
        // Create a JSON object with the text content for the request
        JSONObject json = new JSONObject();
        json.put("content", text);

        // Send initial POST request with current token
        Request request = createPostRequest(json, false);
        JsonObject response = handleResponse(request);

        try {
            // Handle server response based on status code
            if (response.get("status").getAsInt() == 200) {
                // Success case: extract sentiment data and return corresponding SentimentType
                String data = response.get("data").getAsString();
                return SentimentType.valueOf(data.toUpperCase());
            } else if (response.get("status").getAsInt() == 403) {
                // Token expired: retry with a new token
                request = createPostRequest(json, true);
                response = handleResponse(request);
                String data = response.get("data").getAsString();
                return SentimentType.valueOf(data.toUpperCase());
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            log.error("Error occurred while analyzing sentiment", e);
        }

        // Default case for unhandled errors
        return SentimentType.NEUTRAL;
    }

    /**
     * Processes the server response and returns a JsonObject with status and data.
     * @param request The HTTP request sent to the server
     * @return JsonObject containing status code and response data (if any)
     */
    private JsonObject handleResponse(Request request) {
        JsonObject result = new JsonObject();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                // Parse successful response body into JSON
                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String data = jsonNode.get("data").asText();

                // Populate result with data and success status
                result.addProperty("data", data);
                result.addProperty("status", 200);
            } else {
                // Log error for unsuccessful response
                log.error("Error occurred while analyzing sentiment: {}", response.code());
                result.addProperty("status", response.code());
            }
        } catch (IOException | IllegalArgumentException e) {
            // Log exception details if response processing fails
            log.error("Error occurred while analyzing sentiment", e);
            result.addProperty("status", 500); // Default error status
        }
        return result;
    }

    /**
     * Creates a POST request with JSON body and authorization token.
     * @param json JSON object containing the request payload
     * @param isGenerateNewToken Flag to force generation of a new token
     * @return Configured Request object
     */
    private Request createPostRequest(JSONObject json, boolean isGenerateNewToken) {
        // Retrieve or generate token based on the flag
        String token = getToken(isGenerateNewToken);

        // Create request body from JSON string
        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));

        // Build and return the POST request with URL, headers, and body
        return new Request.Builder()
                .url(sentimentServiceUrl + "/predict-sentiment")
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();
    }

    /**
     * Retrieves or generates a JWT token, caching it in Redis.
     * @param isGenerateNewToken If true, forces generation of a new token
     * @return JWT token string
     */
    private String getToken(boolean isGenerateNewToken) {
        String redisKey = RedisKeyUtil.JWT_SENTIMENT_SERVICE;
        String jwt = redisService.getValue(redisKey, String.class);

        // Check if token is missing or needs regeneration
        if (StringUtils.isBlank(jwt) || isGenerateNewToken) {
            try {
                // Decode private key and generate a new token
                String privateKeyDecode = encoder.decode(privateKey).get();
                jwt = jwtService.generateAccessToken("sentiment-service", privateKeyDecode).join();

                // Store token in Redis with expiration time
                redisService.saveValue(redisKey, jwt);
                redisService.setTTL(redisKey, ACCESS_TOKEN_LIFETIME, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException e) {
                // Handle exceptions during token generation, interrupt thread if needed
                Thread.currentThread().interrupt();
                log.error("Error while generating token", e);
                return null; // Return null on failure (consider alternative handling)
            }
        }
        return jwt;
    }
}

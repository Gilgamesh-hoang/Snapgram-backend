package org.snapgram.service.sentiment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONObject;
import org.snapgram.enums.SentimentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SentimentService implements ISentimentService {
    final OkHttpClient client;
    final ObjectMapper objectMapper;

    @Value("${application.sentiment-service.url}")
    String sentimentServiceUrl;

    @Override
    public SentimentType analyzeSentiment(@NotBlank String text) {
        JSONObject json = new JSONObject();
        json.put("content", text);

        // Create a POST request with the JSON object
        Request request = createPostRequest(json);

        try (Response response = client.newCall(request).execute()) {
            // Check if the response is successful and has a body
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                // Parse the response body into a JSON node
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                // Get the sentiment type from the JSON node and return it
                String resp = jsonNode.get("data").asText();
                return SentimentType.valueOf(resp);
            } else {
                log.error("Request failed: {}", response.code());
                return SentimentType.OTHER;
            }
        } catch (IOException | IllegalArgumentException e) {
            log.error("Error occurred while analyzing sentiment", e);
        }
        return SentimentType.OTHER;
    }

    private Request createPostRequest(JSONObject json) {
        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        return new Request.Builder()
                .url(sentimentServiceUrl + "/predict-sentiment")
                .post(body)
                .build();
    }
}

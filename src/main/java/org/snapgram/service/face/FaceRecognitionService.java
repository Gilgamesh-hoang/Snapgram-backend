package org.snapgram.service.face;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONObject;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.service.user.IUserService;
import org.snapgram.util.FaceRecognitionConstant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class FaceRecognitionService implements IFaceRecognitionService {
    final IUserService userService;
    final OkHttpClient client;
    @Value("${application.face-service.url}")
    String faceServiceUrl;

    @Override
    public List<UserDTO> identify(String imageUrl) {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject json = new JSONObject();
        json.put("imageURL", imageUrl);

        Request request = createPostRequest(json, "/identify-face");
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                System.out.println("Response: " + responseBody);
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                int code = jsonNode.get("status_code").asInt();
                if (code == FaceRecognitionConstant.STATUS_CODE_SUCCESS) {
                    UUID[] uuids = objectMapper.convertValue(jsonNode.get("data"), UUID[].class);
                    return userService.getUsersByUUIDs(uuids);
                }
                return List.of();
            } else {
                log.error("Request failed: {}", response.code());
                return List.of();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while identifying face", e);
        }
    }

    private Request createPostRequest(JSONObject json, String endpoint) {
        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        return new Request.Builder()
                .url(faceServiceUrl + endpoint)
                .post(body)
                .build();
    }

    @Override
    @Async
    public CompletableFuture<Void> train(UUID userId, List<String> urls) {
        UserDTO user = userService.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        JSONObject json = new JSONObject();
        json.put("user_id", userId);
        json.put("imageURLs", urls);

        Request request = createPostRequest(json, "/train-images");
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
               log.info("Response from train: {}", responseBody);
            } else {
                log.error("Request failed: {}", response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while identifying face");
        }
        return CompletableFuture.completedFuture(null);
    }
}

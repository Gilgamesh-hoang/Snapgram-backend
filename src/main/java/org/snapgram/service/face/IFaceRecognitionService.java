package org.snapgram.service.face;

import org.snapgram.dto.response.UserDTO;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IFaceRecognitionService {
    List<UserDTO> identify(String imageUrl);

    CompletableFuture<Void> train(UUID userId, List<String> urls);
}

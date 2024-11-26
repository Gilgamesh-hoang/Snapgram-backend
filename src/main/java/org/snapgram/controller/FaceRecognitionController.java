package org.snapgram.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CloudinaryMedia;
import org.snapgram.dto.request.FaceRecognitionRequest;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.service.cloudinary.ICloudinarySignatureService;
import org.snapgram.service.face.IFaceRecognitionService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/faces")
@Validated
public class FaceRecognitionController {
    ICloudinarySignatureService cloudinarySignatureService;
    IFaceRecognitionService faceService;

    @PostMapping("/identify")
    public ResponseObject<List<UserDTO>> createPost(@RequestBody @Valid CloudinaryMedia media) {
        if (!cloudinarySignatureService.verifySignature(media)) {
            throw new IllegalArgumentException("Invalid signature");
        }

        List<UserDTO> users = faceService.identify(media.getUrl());
        if (users.isEmpty()) {
            return new ResponseObject<>(HttpStatus.NOT_FOUND, "No user found", null);
        } else {
            return new ResponseObject<>(HttpStatus.OK, "Users found", users);
        }
    }

    @PostMapping("/trainings")
    public ResponseObject<Void> train(@RequestBody @Valid FaceRecognitionRequest request) {
        List<String> filteredMedia = request.getImages().stream()
                .filter(cloudinarySignatureService::verifySignature).map(CloudinaryMedia::getUrl).toList();

        if (filteredMedia.isEmpty()) {
            return new ResponseObject<>(HttpStatus.BAD_REQUEST, "Invalid signature for images", null);
        }

//        faceService.train(request.getUserId(), filteredMedia);
        return new ResponseObject<>(HttpStatus.OK, "Training started", null);
    }
}

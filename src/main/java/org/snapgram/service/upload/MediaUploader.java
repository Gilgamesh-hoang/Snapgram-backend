package org.snapgram.service.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.snapgram.dto.CloudinaryPojo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaUploader {
    private final Cloudinary cloudinary;
    private final ObjectMapper objectMapper;
    @Value("${cloudinary.folderName}")
    private String folderName;

    public CloudinaryPojo uploadFile(MultipartFile file) throws IOException {
        // Attempt to upload the file to Cloudinary.
        // The file's bytes are passed to the upload method along with an empty configuration Map.
        Map<String, Object> uploadOptions = new HashMap<>();
        uploadOptions.put("folder", folderName);

        // check file type
        String contentType = file.getContentType();
        if (contentType!= null) {
            if (contentType.startsWith("image")) {
                uploadOptions.put("resource_type", "image");
            } else if (contentType.startsWith("video")) {
                uploadOptions.put("resource_type", "video");
            }
        }

        Map<String, String> res = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
        return objectMapper.convertValue(res, CloudinaryPojo.class);

    }

    public void deleteFiles(List<String> publicIds) {
        // Create an ExecutorService with a fixed thread pool of 3 threads.
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Create a list to hold Future objects.
        // These objects represent the result of an asynchronous computation.
        List<Future<?>> futures = new ArrayList<>();

        // For each publicId in the provided list,
        // submit a task to the executor service to delete the file with that publicId.
        for (String publicId : publicIds) {
            if (publicId != null) {
                futures.add(
                        executor.submit(() -> {
                            try {
                                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                            } catch (IOException e) {
                                log.error("Failed to delete file: {}", e.getMessage());
                            }
                        }
                ));
            }
        }

        // For each Future in the list, get the result of the computation.
        futures.forEach(future -> {
            try {
                future.get();
            } catch (CancellationException | ExecutionException | InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Failed to delete file: {}", e.getMessage());
            }
        });

        executor.shutdown();
    }
}

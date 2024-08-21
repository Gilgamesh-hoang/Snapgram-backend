package org.snapgram.service.upload;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MediaUploader {
    private final Cloudinary cloudinary;
    @Value("${cloudinary.folderName}")
    private String folderName;

    public String uploadFile(MultipartFile file) throws IOException {
        // Attempt to upload the file to Cloudinary.
        // The file's bytes are passed to the upload method along with an empty configuration Map.
        Map<String, Object> uploadOptions = new HashMap<>();
        uploadOptions.put("folder", folderName);

        // check file type
        String contentType = file.getContentType();
        if (contentType.startsWith("image")) {
            uploadOptions.put("resource_type", "image");
        } else if (contentType.startsWith("video")) {
            uploadOptions.put("resource_type", "video");
        }

        Map res = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
        return res.get("url").toString();

    }
}

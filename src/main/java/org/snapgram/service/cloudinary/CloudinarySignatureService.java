package org.snapgram.service.cloudinary;

import com.cloudinary.Cloudinary;
import com.fasterxml.uuid.Generators;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.CloudinarySignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CloudinarySignatureService implements ICloudinarySignatureService{
    final Cloudinary cloudinary;
    @Value("${cloudinary.api_secret}")
    String apiSecret;
    @Value("${cloudinary.api_key}")
    String apiKey;
    @Value("${cloudinary.folderName}")
    String folderName;

    @Override
    public List<CloudinarySignature> generateSignature(int sigNums) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        List<Future<CloudinarySignature>> futures = new ArrayList<>();

        for (int i = 0; i < sigNums; i++) {
            futures.add(executorService.submit(() -> {

                long timestamp = System.currentTimeMillis() / 1000;

                String publicId = "file_" + Generators.randomBasedGenerator().generate();

                // Tham số cần ký
                Map<String, Object> paramsToSign = new HashMap<>();
                paramsToSign.put("timestamp", timestamp);
                paramsToSign.put("folder", folderName);
                paramsToSign.put("public_id", publicId);

                String signature = cloudinary.apiSignRequest(paramsToSign, apiSecret);
                return new CloudinarySignature(apiKey, timestamp, folderName, signature, publicId);
            }));
        }
        List<CloudinarySignature> result = new ArrayList<>(sigNums);

        for (Future<CloudinarySignature> future : futures) {
            result.add(future.get());
        }
        executorService.shutdown();
        return result;
    }

    @Override
    public boolean verifySignature(String publicId, String version, String signature) {
        return cloudinary.verifyApiResponseSignature(publicId, version, signature);
    }
}

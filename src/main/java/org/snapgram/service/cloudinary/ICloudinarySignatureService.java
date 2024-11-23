package org.snapgram.service.cloudinary;

import org.snapgram.dto.response.CloudinarySignature;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ICloudinarySignatureService {
    List<CloudinarySignature> generateSignature(int sigNums) throws ExecutionException, InterruptedException;

    boolean verifySignature(String publicId, String version, String signature);
}

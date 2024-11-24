package org.snapgram.service.cloudinary;

import org.snapgram.dto.CloudinaryMedia;
import org.snapgram.dto.response.CloudinarySignature;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ICloudinarySignatureService {
    List<CloudinarySignature> generateSignature(int sigNums) throws ExecutionException, InterruptedException;

    boolean verifySignature(CloudinaryMedia media);
}

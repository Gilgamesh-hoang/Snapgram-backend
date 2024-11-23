package org.snapgram.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.CloudinarySignature;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.service.cloudinary.ICloudinarySignatureService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@RequestMapping("${API_PREFIX}/cloudinary")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CloudinaryController {
    ICloudinarySignatureService cloudinarySignatureService;

    @PostMapping("/signature/{sigNums}")
    public ResponseObject<List<CloudinarySignature>> generateSignature(@PathVariable("sigNums") @Max(20) @Min(1) Integer sigNums)   {
        try {
            return new ResponseObject<>(HttpStatus.OK, "Signatures generated",
                    cloudinarySignatureService.generateSignature(sigNums));
        } catch (ExecutionException | InterruptedException e) {
            /* Clean up whatever needs to be handled before interrupting  */
            Thread.currentThread().interrupt();
            return new ResponseObject<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error while generating signature", null);
        }
    }

}

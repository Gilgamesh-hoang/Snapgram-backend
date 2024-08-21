package org.snapgram.annotation.media;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class MediaValidator implements ConstraintValidator<ValidMedia, MultipartFile[]> {

    private int maxFiles;
    private long maxFileSize;

    @Override
    public void initialize(ValidMedia constraintAnnotation) {
        this.maxFiles = constraintAnnotation.maxFiles();
        this.maxFileSize = constraintAnnotation.maxFileSize();
    }

    @Override
    public boolean isValid(MultipartFile[] media, ConstraintValidatorContext context) {
        if (media == null) {
            return true;  // Consider null as valid (no files provided)
        }

        if (media.length > maxFiles) {
            // Disable default message
            context.disableDefaultConstraintViolation();
            // Add a custom message
            context.buildConstraintViolationWithTemplate("You can only upload up to " + maxFiles + " files.")
                    .addConstraintViolation();
            return false;
        }

        for (MultipartFile file : media) {
            if (file.getSize() > maxFileSize) {
                // Disable default message
                context.disableDefaultConstraintViolation();
                // Add a custom message
                context.buildConstraintViolationWithTemplate("File " + file.getOriginalFilename() + " exceeds the maximum allowed size of " + (maxFileSize / (1024 * 1024)) + " MB.")
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}

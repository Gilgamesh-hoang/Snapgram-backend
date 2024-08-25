package org.snapgram.annotation.media;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class MediaValidator implements ConstraintValidator<ValidMedia, Object> {

    private int maxFiles;
    private long maxFileSize;

    @Override
    public void initialize(ValidMedia constraintAnnotation) {
        this.maxFiles = constraintAnnotation.maxFiles();
        this.maxFileSize = constraintAnnotation.maxFileSize();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Consider null as valid (no file provided)
        }

        if (value instanceof MultipartFile[] media) {
            return validateFiles(media, context);
        } else if (value instanceof MultipartFile file) {
            return validateFile(file, context);
        } else {
            return false; // Invalid type
        }
    }

    private boolean validateFiles(MultipartFile[] media, ConstraintValidatorContext context) {
        if (media.length > maxFiles) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("You can only upload up to " + maxFiles + " files.")
                    .addConstraintViolation();
            return false;
        }

        for (MultipartFile file : media) {
            if (!validateFile(file, context)) {
                return false;
            }
        }

        return true;
    }

    private boolean validateFile(MultipartFile file, ConstraintValidatorContext context) {
        if (file.getSize() > maxFileSize) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("File " + file.getOriginalFilename() + " exceeds the maximum allowed size of " + (maxFileSize / (1024 * 1024)) + " MB.")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}

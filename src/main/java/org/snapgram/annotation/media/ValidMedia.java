package org.snapgram.annotation.media;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for validating media files in a request.
 * This annotation is used to validate the number and size of media files in a request.
 * It uses the MediaValidator class for the validation logic.
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD }) // This annotation can be used on parameters and fields
@Retention(RetentionPolicy.RUNTIME) // This annotation is available at runtime
@Constraint(validatedBy = MediaValidator.class) // This annotation uses the MediaValidator class for validation
public @interface ValidMedia {

    /**
     * Default error message when the validation fails.
     */
    String message() default "Invalid media files.";

    /**
     * Default validation groups.
     */
    Class<?>[] groups() default {};

    /**
     * Default payload.
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * Maximum number of files allowed.
     * Default is 15 files.
     */
    int maxFiles() default 15;

    /**
     * Maximum size for each file.
     * Default is 25MB.
     */
    long maxFileSize() default 25 * 1024 * 1024;
}

package org.snapgram.annotation.tag;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TagsValidator implements ConstraintValidator<ValidTags, List<String>> {

    private int maxTags;
    private int maxTagLength;

    @Override
    public void initialize(ValidTags constraintAnnotation) {
        this.maxTags = constraintAnnotation.maxTags();
        this.maxTagLength = constraintAnnotation.maxTagLength();
    }

    @Override
    public boolean isValid(List<String> tags, ConstraintValidatorContext context) {
        if (tags == null) {
            return true;  // Consider null as valid (no tags provided)
        }

        if (tags.size() > maxTags) {
            // Disable default message
            context.disableDefaultConstraintViolation();
            // Add a custom message
            context.buildConstraintViolationWithTemplate("You can only add up to " + maxTags + " tags.")
                    .addConstraintViolation();
            return false;
        }

        Set<String> uniqueTags = new HashSet<>();
        for (String tag : tags) {
            if (tag.trim().length() > maxTagLength) {
                // Disable default message
                context.disableDefaultConstraintViolation();
                // Add a custom message
                context.buildConstraintViolationWithTemplate("Each tag can only have up to " + maxTagLength + " characters.")
                        .addConstraintViolation();
                return false;
            }
            if (!uniqueTags.add(tag.trim().toLowerCase())) {
                // Disable default message
                context.disableDefaultConstraintViolation();
                // Add a custom message
                context.buildConstraintViolationWithTemplate("Tags must be unique.")
                        .addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
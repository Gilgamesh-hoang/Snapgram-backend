package org.snapgram.validation.tag;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = TagsValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTags {
    String message() default "Invalid tags";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int maxTags() default 20;
    int maxTagLength() default 20;
}

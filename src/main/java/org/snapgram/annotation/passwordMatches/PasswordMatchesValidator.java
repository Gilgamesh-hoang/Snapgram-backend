package org.snapgram.annotation.passwordMatches;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.snapgram.model.request.SignupRequest;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, SignupRequest> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
    }

    @Override
    public boolean isValid(SignupRequest signupRequest, ConstraintValidatorContext context) {
        return signupRequest.getPassword().equals(signupRequest.getConfirmPassword());
    }
}

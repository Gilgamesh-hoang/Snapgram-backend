package org.snapgram.annotation.password_matches;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.snapgram.model.request.SignupRequest;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, SignupRequest> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) { // default implementation ignored
    }

    @Override
    public boolean isValid(SignupRequest signupRequest, ConstraintValidatorContext context) {
        return signupRequest.getPassword().equals(signupRequest.getConfirmPassword());
    }
}

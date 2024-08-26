package org.snapgram.validation.password_matches;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.snapgram.dto.request.ChangePasswordRequest;
import org.snapgram.dto.request.SignupRequest;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) { // default implementation ignored
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        String password;
        String confirmPassword;

        // Use reflection to get the appropriate fields
        if (obj instanceof SignupRequest request) {
            password = request.getPassword();
            confirmPassword = request.getConfirmPassword();
        } else if (obj instanceof ChangePasswordRequest request) {
            password = request.getNewPassword();
            confirmPassword = request.getConfirmNewPassword();
        } else {
            return false;
        }
        return password.equals(confirmPassword);
    }
}

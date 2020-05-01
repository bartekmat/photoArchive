package com.photoarchive.validators;

import com.photoarchive.annotations.StrongPassword;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        boolean matches = s.matches("^(?=.*[0-9])(?=.*[a-zA-Z])(?=\\S+$).{5,15}$");
        System.out.println("matches = " + matches);
        return matches;
    }
}

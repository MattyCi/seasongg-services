package com.sgg.users;

import javax.validation.Constraint;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Constraint(validatedBy = { UserRegistrationValidator.class })
public @interface ValidUserRegistration {

    String message() default "The supplied user registration information was incorrect.";

}

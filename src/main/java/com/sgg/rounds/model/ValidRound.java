package com.sgg.rounds.model;

import jakarta.validation.Constraint;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ ANNOTATION_TYPE, TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = RoundValidator.class)
@Documented
public @interface ValidRound {
    String message() default "The round was invalid.";
    Class<?>[] groups() default {};
}

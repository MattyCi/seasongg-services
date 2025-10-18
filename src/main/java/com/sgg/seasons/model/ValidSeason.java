package com.sgg.seasons.model;

import jakarta.validation.Constraint;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ ANNOTATION_TYPE, TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = SeasonValidator.class)
@Documented
public @interface ValidSeason {
    String message() default "The season was invalid.";
    Class<?>[] groups() default {};
}

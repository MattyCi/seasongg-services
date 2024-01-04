package com.sgg.users;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This class cannot be used until
 * <a href="https://github.com/micronaut-projects/micronaut-validation/issues/258">
 *     https://github.com/micronaut-projects/micronaut-validation/issues/258</a> is resolved
 */
@Slf4j
@Singleton
@Introspected
@AllArgsConstructor(onConstructor_ = @Inject)
class UserRegistrationValidator implements ConstraintValidator<ValidUserRegistration, Object> {

    private UserRepository userRepository;

    // TODO: use resource bundle instead
    private static final String USERNAME_ALREADY_EXISTS_ERROR_TEXT = "The username provided is already in use.";

    @Override
    public boolean isValid(Object value, @NonNull AnnotationValue<ValidUserRegistration> annotationMetadata,
                           @NonNull ConstraintValidatorContext context) {

        val userRegistrationRequest = (UserRegistrationRequest) value;

        if (userRepository.findByUsernameIgnoreCase(userRegistrationRequest.getUsername()).isPresent()) {
            log.info("user registration failed. username {} already exists",
                    userRegistrationRequest.getUsername());
            context.messageTemplate(USERNAME_ALREADY_EXISTS_ERROR_TEXT);
            return false;
        }

        return true;
    }

}

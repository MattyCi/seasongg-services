package com.sgg.seasons.model;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext;
import jakarta.inject.Singleton;

@Singleton
@Introspected
public class SeasonValidator implements ConstraintValidator<ValidSeason, SeasonDto> {

    @Override
    public boolean isValid(@Nullable SeasonDto season, @NonNull AnnotationValue<ValidSeason> annotationMetadata,
                           @NonNull ConstraintValidatorContext context) {
        return validateDates(season, context);
    }

    private static boolean validateDates(SeasonDto season, ConstraintValidatorContext context) {
        // TODO: shouldn't the start date be set to now ???
        if (season.getStartDate() == null) {
            context.messageTemplate("{season.startDate.NotNull}");
            return false;
        }

        if (season.getEndDate() == null) {
            context.messageTemplate("{season.endDate.NotNull}");
            return false;
        }

        if (season.getEndDate().isBefore(season.getStartDate())) {
            context.messageTemplate("Please choose a date in the future for your season end date.");
            return false;
        }
        return true;
    }
}

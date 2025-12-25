package com.sgg.rounds.model;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Singleton
@Introspected
public class RoundValidator implements ConstraintValidator<ValidRound, RoundDto> {


    @Override
    public boolean isValid(RoundDto round, @NonNull AnnotationValue<ValidRound> annotationMetadata,
                           @NonNull ConstraintValidatorContext context) {
        if (round.getRoundResults() == null) {
            context.messageTemplate("{round.roundResults.NotNull}");
            return false;
        }
        return areRoundResultsInOrder(round.getRoundResults(), context);
    }

    private boolean areRoundResultsInOrder(List<RoundResultDto> results, ConstraintValidatorContext context) {
        // 1) figure out what was last place, so you know how many places to count down from
        Integer lastPlace = 0;
        List<Integer> places = results.stream().map(RoundResultDto::getPlace).toList();
        for (Integer place : places) {
            if (place > lastPlace) {
                lastPlace = place;
            }
        }

        // 2) counting down from last place, check to make sure that no places were "skipped" or missing
        for (int i = lastPlace; i > 0; i--) {
            if (!places.contains(i)) {
                context.messageTemplate("{round.result.place.Ordering}");
                return false;
            }
        }
        return true;
    }
}

package com.sgg.rounds.model;

import com.sgg.users.model.UserDto;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
        if (duplicatePlayersInRoundResults(round.getRoundResults())) {
            context.messageTemplate("{round.result.user.Duplicate}");
            return false;
        }
        List<Integer> places = round.getRoundResults().stream().map(RoundResultDto::getPlace).toList();
        if (placesMissing(determineLastPlace(places), places)) {
            context.messageTemplate("{round.result.place.Ordering}");
            return false;
        } else {
            return true;
        }
    }

    private static boolean duplicatePlayersInRoundResults(List<RoundResultDto> results) {
        val userIds = results.stream().map(RoundResultDto::getUser).map(UserDto::getUserId).toList();
        return userIds.stream().distinct().count() != userIds.size();
    }

    private static Integer determineLastPlace(List<Integer> places) {
        Integer lastPlace = 0;
        for (Integer place : places) {
            if (place > lastPlace) {
                lastPlace = place;
            }
        }
        return lastPlace;
    }

    private static boolean placesMissing(Integer lastPlace, List<Integer> places) {
        for (int i = lastPlace; i > 0; i--) {
            if (!places.contains(i)) {
                return true;
            }
        }
        return false;
    }
}

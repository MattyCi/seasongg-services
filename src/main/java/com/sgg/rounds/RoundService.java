package com.sgg.rounds;

import com.sgg.common.exception.ClientException;
import com.sgg.common.exception.NotFoundException;
import com.sgg.rounds.model.RoundDao;
import com.sgg.rounds.model.RoundDto;
import com.sgg.rounds.model.RoundResultDao;
import com.sgg.seasons.SeasonService;
import com.sgg.seasons.model.SeasonStatus;
import com.sgg.users.UserService;
import com.sgg.users.model.UserDto;
import io.micronaut.transaction.annotation.Transactional;
import io.micronaut.validation.validator.Validator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Singleton
@Slf4j
@AllArgsConstructor(onConstructor_ = @Inject)
public class RoundService {
    private final RoundRepository roundRepository;
    private final UserService userService;
    private final RoundMapper roundMapper;
    private final Validator validator;
    private final SeasonService seasonService;

    @Transactional(readOnly = true)
    public RoundDto getRound(Long id) {
        val round = roundRepository.findById(id);
        if (round.isEmpty()) {
            throw new NotFoundException("No round was found for the given ID.");
        } else {
            return roundMapper.toRoundDto(round.get());
        }
    }

    @Transactional
    public RoundDto createRound(String seasonId, RoundDto round) {
        // TODO: calculate points for round results based on place
        // TODO: check for multiple players in a round result (ie. a player should only be in a round once)
        // TODO: add test to make sure massive number of RR can't be added
        // TODO: validate places (ie. 1st, 2nd, 4th ... should throw exception since 3rd was skipped ? or allow for tie?
        val season = seasonService.getSeason(seasonId); // will throw if season doesn't exist
        if (season.getStatus() != SeasonStatus.ACTIVE) {
            throw new ClientException("Rounds cannot be created because the season has ended.");
        }
        round.setRoundDate(OffsetDateTime.now(ZoneId.of("America/New_York")));
        round.setSeason(season);
        round.setCreator(userService.getCurrentUser());
        validateRound(round);
        validatePlayers(round);
        val roundDao = associateResultsToRoundDao(round);
        val saved = roundRepository.save(roundDao);
        return roundMapper.toRoundDto(saved);
    }

    private void validateRound(RoundDto round) {
        val violations = validator.validate(round);
        if (!violations.isEmpty()) {
            throw new ClientException(violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(" "))
            );
        }
    }

    // TODO: you don't need to iterate twice ... call this from the below method
    private void validatePlayers(RoundDto round) {
        round.getRoundResults().forEach(roundResult -> {
            UserDto player;
            try {
                player = userService.getUserById(roundResult.getUser().getUserId());
            } catch (NotFoundException ignored) {
                val msg = String.format("Failed to create round because the player %s in place %s does not exist.",
                        roundResult.getUser().getUsername(), roundResult.getPlace());
                log.error(msg);
                throw new ClientException(msg);
            }
            roundResult.setUser(player);
        });
    }

    @Nonnull
    private RoundDao associateResultsToRoundDao(RoundDto round) {
        val roundDao = roundMapper.toRoundDao(round);
        roundDao.setRoundResults(new ArrayList<>());
        round.getRoundResults().stream()
                .map(roundMapper::toRoundResultDao)
                .peek(rr -> rr.setRound(roundDao))
                .peek(rr -> logRoundResult(roundDao.getSeason().getName(), rr))
                .forEach(roundDao::addRoundResult);
        return roundDao;
    }

    private static void logRoundResult(String seasonName, RoundResultDao roundResult) {
        log.info("adding season {} round result for player {} who came in place {}",
                seasonName, roundResult.getUser().getUsername(), roundResult.getPlace());
    }
}

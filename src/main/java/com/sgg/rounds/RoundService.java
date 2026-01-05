package com.sgg.rounds;

import com.sgg.common.exception.ClientException;
import com.sgg.common.exception.NotFoundException;
import com.sgg.rounds.model.RoundDao;
import com.sgg.rounds.model.RoundDto;
import com.sgg.rounds.model.RoundResultDao;
import com.sgg.rounds.model.RoundResultDto;
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
        // TODO: recalculate season standings upon round creation
        val season = seasonService.getSeason(seasonId);
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

    private RoundDao associateResultsToRoundDao(RoundDto round) {
        val roundDao = roundMapper.toRoundDao(round);
        roundDao.setRoundResults(new ArrayList<>());
        round.getRoundResults().stream()
                .peek(this::calculatePoints)
                .map(roundMapper::toRoundResultDao)
                .peek(rr -> rr.setRound(roundDao))
                .peek(rr -> logRoundResult(roundDao.getSeason().getName(), rr))
                .forEach(roundDao::addRoundResult);
        return roundDao;
    }

    private void calculatePoints(RoundResultDto result) {
        // TODO: for now, basic scoring algorithm. in future multiple possible scoring systems allowed
        double points;
        int place = result.getPlace();
        points = switch (place) {
            case 1 -> 10;
            case 2 -> 9;
            case 3 -> 8;
            case 4 -> 7;
            case 5 -> 6;
            case 6 -> 5;
            case 7 -> 4;
            case 8 -> 3;
            case 9 -> 2;
            default -> 1;
        };
        result.setPoints(points);
    }

    private static void logRoundResult(String seasonName, RoundResultDao roundResult) {
        log.info("adding season {} round result for player {} who came in place {}",
                seasonName, roundResult.getUser().getUsername(), roundResult.getPlace());
    }

    @Transactional
    public void deleteRound(String seasonId, String roundId) {
        // TODO: recalculate season standings after round deletion
        // TODO: if any users no longer in season, remove their season permissions
        seasonService.removeRound(seasonId, roundId);
        log.info("user {} deleted round with id {} for season {}",
                userService.getCurrentUser().getUsername(), roundId, seasonId);
    }
}

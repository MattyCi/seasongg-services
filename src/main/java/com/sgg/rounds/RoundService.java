package com.sgg.rounds;

import com.sgg.common.exception.ClientException;
import com.sgg.common.exception.NotFoundException;
import com.sgg.rounds.model.RoundDto;
import com.sgg.rounds.model.RoundResultDao;
import com.sgg.rounds.model.RoundResultDto;
import com.sgg.seasons.SeasonService;
import com.sgg.seasons.model.SeasonStatus;
import com.sgg.users.UserDao;
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
        if (round.getSeason().getStatus() != SeasonStatus.ACTIVE) {
            throw new ClientException("Rounds cannot be created because the season has ended.");
        }
        round.setSeason(seasonService.getSeason(seasonId));
        round.setCreator(userService.getCurrentUser());
        validateRound(round);
        // TODO: calculate points for round results based on place
        validatePlayers(round);
        val roundDao = roundMapper.toRoundDao(round);
        roundDao.setRoundResults(new ArrayList<>());
        for(RoundResultDto r : round.getRoundResults()) {
            val rrDao = roundMapper.toRoundResultDao(r);
            rrDao.setRound(roundDao);
            roundDao.addRoundResult(rrDao);
        }
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
            log.info("adding season {} round result for player {} who came in place {}",
                    round.getSeason().getName(), player.getUsername(), roundResult.getPlace());
            roundResult.setUser(player);
        });
    }
}

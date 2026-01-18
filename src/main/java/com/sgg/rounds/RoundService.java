package com.sgg.rounds;

import com.sgg.common.exception.NotFoundException;
import com.sgg.rounds.model.RoundDto;
import com.sgg.seasons.SeasonService;
import com.sgg.seasons.model.SeasonDto;
import com.sgg.users.UserService;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Singleton
@Slf4j
@AllArgsConstructor(onConstructor_ = @Inject)
public class RoundService {
    private final RoundRepository roundRepository;
    private final UserService userService;
    private final RoundMapper roundMapper;
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
    public SeasonDto addRound(String seasonId, RoundDto round) {
        return seasonService.addRound(seasonId, round);
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

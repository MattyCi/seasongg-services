package com.sgg.seasons;

import com.sgg.common.exception.ClientException;
import com.sgg.common.exception.SggException;
import com.sgg.games.GameRepository;
import com.sgg.games.model.GameDto;
import com.sgg.games.model.GameMapper;
import com.sgg.seasons.model.SeasonDto;
import com.sgg.seasons.model.SeasonMapper;
import com.sgg.seasons.model.SeasonStatus;
import com.sgg.users.UserDao;
import com.sgg.users.UserService;
import com.sgg.users.model.UserDto;
import io.micronaut.security.utils.SecurityService;
import io.micronaut.validation.validator.Validator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Singleton
@Slf4j
@AllArgsConstructor(onConstructor_ = @Inject)
public class SeasonService {
    public static final String ERR_MUST_BE_AUTHENTICATED = "You must be logged in to create a season.";
    private final SeasonRepository seasonRepository;
    private final GameRepository gameRepository;
    private final SecurityService securityService;
    private final UserService userService;
    private final SeasonMapper seasonMapper;
    private final GameMapper gameMapper;
    private final Validator validator;

    @Transactional
    public SeasonDto createSeason(SeasonDto season) {
        // TODO: we need to validate the game first
        maybePersistGame(season.getGame());
        initSeason(season);
        val violations = validator.validate(season);
        if (!violations.isEmpty()) {
            throw new ClientException(violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "))
            );
        }
        season.setName(season.getName().trim());

        seasonRepository.save(seasonMapper.toSeasonDao(season));
        return season;
    }

    private void maybePersistGame(GameDto game) {
        if (gameRepository.findById(game.getGameId()).isEmpty())
            gameRepository.save(gameMapper.toGameDao(game));
    }

    private void initSeason(SeasonDto season) {
        season.setCreator(getCreator());
        season.setStatus(SeasonStatus.ACTIVE);
        season.setStartDate(OffsetDateTime.now());
    }

    private UserDto getCreator() {
       return securityService.getAuthentication()
               .orElseThrow(() -> new SggException(ERR_MUST_BE_AUTHENTICATED))
               .getAttributes()
               .entrySet().stream()
               .filter((e) -> "userId".equals(e.getKey()))
               .map((e) -> userService.getUserById(Long.valueOf(e.getValue().toString())))
               .findFirst()
               .orElseThrow(() -> new SggException("Unexpected error occurred trying to retrieve current user."));
    }
}

package com.sgg.seasons;

import com.sgg.common.exception.ClientException;
import com.sgg.common.exception.NotFoundException;
import com.sgg.common.exception.SggException;
import com.sgg.games.GameRepository;
import com.sgg.games.model.GameDto;
import com.sgg.games.model.GameMapper;
import com.sgg.seasons.model.SeasonDto;
import com.sgg.seasons.model.SeasonMapper;
import com.sgg.seasons.model.SeasonStatus;
import com.sgg.users.UserMapper;
import com.sgg.users.UserService;
import com.sgg.users.authz.*;
import com.sgg.users.model.UserDto;
import io.micronaut.security.utils.SecurityService;
import io.micronaut.validation.validator.Validator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.validation.ConstraintViolation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;

@Singleton
@Slf4j
@AllArgsConstructor(onConstructor_ = @Inject)
public class SeasonService {
    private static final String ERR_MUST_BE_AUTHENTICATED = "You must be logged in to create a season.";
    private final SeasonRepository seasonRepository;
    // TODO: refactor this - game repo should be encapsulated in a service
    private final GameRepository gameRepository;
    private final SecurityService securityService;
    private final UserService userService;
    private final SeasonMapper seasonMapper;
    private final GameMapper gameMapper;
    private final Validator validator;
    private final UserMapper userMapper;
    private final PermissionService permissionService;

    @Transactional(readOnly = true)
    public SeasonDto getSeason(String id) {
        val season = seasonRepository.findById(parseSeasonId(id));
        if (season.isEmpty()) {
            throw new NotFoundException("No season was found for the given ID.");
        } else {
            return seasonMapper.toSeasonDto(season.get());
        }
    }

    @Transactional
    public SeasonDto createSeason(SeasonDto season) {
        val creator = getCreator();
        initSeason(season, creator);
        validateSeason(season);
        season.setName(season.getName().trim());
        maybePersistGame(season.getGame());
        if (seasonRepository.findByNameIgnoreCase(season.getName()).isPresent())
            throw new ClientException("A season with that name already exists.");
        if (season.getRounds() != null)
            throw new ClientException("Seasons cannot have rounds upon creation.");
        val persistedSeason = seasonRepository.save(seasonMapper.toSeasonDao(season));
        permissionService.insertSeasonAdminPermission(persistedSeason.getSeasonId(), userMapper.userDtoToUser(creator));
        return seasonMapper.toSeasonDto(persistedSeason);
    }

    private void validateSeason(SeasonDto season) {
        val violations = validator.validate(season);
        if (!violations.isEmpty()) {
            throw new ClientException(violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(" "))
            );
        }
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

    private void initSeason(SeasonDto season, UserDto creator) {
        season.setCreator(creator);
        season.setStatus(SeasonStatus.ACTIVE);
        season.setStartDate(OffsetDateTime.now(ZoneId.of("America/New_York")));
    }

    // TODO: we need to do some kind of validation here
    private void maybePersistGame(GameDto game) {
        if (gameRepository.findById(game.getGameId()).isEmpty())
            gameRepository.save(gameMapper.toGameDao(game));
    }

    @Transactional
    public SeasonDto updateSeason(String id, SeasonDto updatedSeason) {
        validateSeason(updatedSeason);
        val storedSeason = seasonRepository.findById(parseSeasonId(id)).map(seasonMapper::toSeasonDto)
                .orElseThrow(() -> new NotFoundException("No season was found for the given ID."));
        if (!storedSeason.getCreator().getUserId().equals(updatedSeason.getCreator().getUserId())) {
            updateSeasonAdmin(updatedSeason, storedSeason);
        }
        if (!updatedSeason.getEndDate().equals(storedSeason.getEndDate())) {
            updateSeasonEndDate(updatedSeason, storedSeason);
        }
        val updatedStoredSeason = seasonRepository.update(seasonMapper.toSeasonDao(storedSeason));
        return seasonMapper.toSeasonDto(updatedStoredSeason);
    }

    private void updateSeasonAdmin(SeasonDto updatedSeason, SeasonDto storedSeason) {
        UserDto newAdmin;
        try {
            newAdmin = userService.getUserById(updatedSeason.getCreator().getUserId());
        } catch (NotFoundException e) {
            throw new NotFoundException("The given user for the season admin does not exist.");
        }
        log.info("Season {} is being updated with a new admin from {} to {}.", storedSeason.getSeasonId(),
                storedSeason.getCreator().getUserId(), updatedSeason.getCreator().getUserId());
        permissionService.swapSeasonAdmins(storedSeason.getSeasonId(),
                userMapper.userDtoToUser(storedSeason.getCreator()), userMapper.userDtoToUser(newAdmin));
        storedSeason.setCreator(newAdmin);
    }

    private void updateSeasonEndDate(SeasonDto updatedSeason, SeasonDto storedSeason) {
        if (updatedSeason.getEndDate().isBefore(
                OffsetDateTime.from(Instant.now().atZone(ZoneId.of("America/New_York"))))) {
            log.info("Season {} has ended due to date update.", storedSeason.getSeasonId());
            storedSeason.setStatus(SeasonStatus.INACTIVE);
        } else if (storedSeason.getStatus().equals(SeasonStatus.INACTIVE)) {
            log.info("Season {} set to active due to date update.", storedSeason.getSeasonId());
            storedSeason.setStatus(SeasonStatus.ACTIVE);
        }
        storedSeason.setEndDate(updatedSeason.getEndDate());
    }

    private Long parseSeasonId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new ClientException("Invalid seasonId provided.");
        }
    }
}

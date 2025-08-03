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

import java.time.OffsetDateTime;
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
    // TODO: refactor this - both of these things should be in a service
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;

    @Transactional(readOnly = true)
    public SeasonDto getSeason(String id) {
        long parsedId;
        try {
            parsedId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new ClientException("Invalid seasonId provided.");
        }
        val season = seasonRepository.findById(parsedId);
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
        val violations = validator.validate(season);
        if (!violations.isEmpty()) {
            throw new ClientException(violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(" "))
            );
        }
        season.setName(season.getName().trim());

        maybePersistGame(season.getGame());

        if (seasonRepository.findByNameIgnoreCase(season.getName()).isPresent())
            throw new ClientException("A season with that name already exists.");

        if (season.getRounds() != null)
            throw new ClientException("Seasons cannot have rounds upon creation.");

        val persistedSeason = seasonRepository.save(seasonMapper.toSeasonDao(season));

        val permission = PermissionDao.builder()
                .permissionType(PermissionType.WRITE)
                .resourceId(persistedSeason.getSeasonId())
                .resourceType(ResourceType.SEASON)
                .build();

        val userPermission = UserPermissionDao.builder()
                .permissionDao(permission)
                .userDao(userMapper.userDtoToUser(creator))
                .build();

        permissionRepository.save(permission);

        userPermissionRepository.save(userPermission);

        return season;
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
        season.setStartDate(OffsetDateTime.now());
    }

    private void maybePersistGame(GameDto game) {
        if (gameRepository.findById(game.getGameId()).isEmpty())
            gameRepository.save(gameMapper.toGameDao(game));
    }
}

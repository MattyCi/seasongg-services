package com.sgg.users;

import com.sgg.games.GameRepository;
import com.sgg.games.model.GameDao;
import com.sgg.seasons.SeasonRepository;
import com.sgg.seasons.model.SeasonDao;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Singleton
@Requires(env = "local")
public class LocalDataBootstrap {

    private final UserService userService;
    private final UserRepository userRepository;
    private final SeasonRepository seasonRepository;
    private final GameRepository gameRepository;

    @Inject
    LocalDataBootstrap(UserService userService,
                       UserRepository userRepository,
                       SeasonRepository seasonRepository,
                       GameRepository gameRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.seasonRepository = seasonRepository;
        this.gameRepository = gameRepository;
    }

    @EventListener
    void onStartup(StartupEvent event) {
        log.info("loading local user bootstrapping data...");
        createTestUser("test-user-1");
        createTestUser("test-user-2");
        createTestUser("test-user-3");

        createTestSeasons();
    }

    private void createTestUser(String username) {
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(
                username,
                "Password123",
                "Password123"
        );
        userService.registerUser(registrationRequest);
    }

    private void createTestSeasons() {
        createGameIfNotExists(100L, "Catan");
        createGameIfNotExists(101L, "Wingspan");
        createGameIfNotExists(102L, "Dominion");

        createSeasonForUser("test-user-1", "Catan Summer Season", 100L);
        createSeasonForUser("test-user-2", "Wingspan Winter Season", 101L);
        createSeasonForUser("test-user-3", "Dominion 2020", 102L);
    }

    private void createGameIfNotExists(Long gameId, String name) {
        if (gameRepository.findById(gameId).isEmpty()) {
            GameDao game = GameDao.builder()
                    .gameId(gameId)
                    .name(name)
                    .seasons(new ArrayList<>())
                    .build();
            gameRepository.save(game);
            log.info("created local game {} ({})", name, gameId);
        }
    }

    private void createSeasonForUser(String username, String seasonName, Long gameId) {
        try {
            if (seasonRepository.findByNameIgnoreCase(seasonName).isPresent()) {
                log.info("season {} already exists, skipping", seasonName);
                return;
            }

            Optional<com.sgg.users.UserDao> userOpt = userRepository.findByUsernameIgnoreCase(username);
            if (userOpt.isEmpty()) {
                log.warn("user {} not found, cannot create season {}", username, seasonName);
                return;
            }

            var userDao = userOpt.get();
            var game = gameRepository.findById(gameId).orElseThrow();

            SeasonDao season = SeasonDao.builder()
                    .name(seasonName)
                    .endDate(OffsetDateTime.now(ZoneId.of("America/New_York")).plusDays(90))
                    .creator(userDao)
                    .status(com.sgg.seasons.model.SeasonStatus.ACTIVE.toString())
                    .rounds(new ArrayList<>())
                    .standings(new ArrayList<>())
                    .game(game)
                    .build();

            seasonRepository.save(season);
            log.info("created local season {} for user {}", seasonName, username);
        } catch (Exception e) {
            log.error("failed to create test season {}: {}", seasonName, e.getMessage(), e);
        }
    }
}

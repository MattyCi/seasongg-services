package com.sgg.users;

import com.sgg.games.GameRepository;
import com.sgg.games.model.GameDao;
import com.sgg.rounds.model.RoundDao;
import com.sgg.rounds.model.RoundResultDao;
import com.sgg.seasons.SeasonRepository;
import com.sgg.seasons.model.SeasonDao;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import io.micronaut.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
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
        createTestRounds();
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

    @Transactional
    private void createTestRounds() {
        try {
            Optional<com.sgg.users.UserDao> user1Opt = userRepository.findByUsernameIgnoreCase("test-user-1");
            Optional<com.sgg.users.UserDao> user2Opt = userRepository.findByUsernameIgnoreCase("test-user-2");
            Optional<com.sgg.users.UserDao> user3Opt = userRepository.findByUsernameIgnoreCase("test-user-3");

            if (user1Opt.isEmpty() || user2Opt.isEmpty()) {
                log.warn("test users not found; skipping round creation");
                return;
            }

            var user1 = user1Opt.get();
            var user2 = user2Opt.get();
            var user3 = user3Opt.orElse(null);

            List<SeasonDao> seasons = new ArrayList<>(seasonRepository.findAll());

            for (SeasonDao season : seasons) {
                // skip if rounds already exist
                if (season.getRounds() != null && !season.getRounds().isEmpty()) {
                    log.info("season {} already has rounds, skipping", season.getName());
                    continue;
                }

                boolean includeUser3 = "Dominion 2020".equalsIgnoreCase(season.getName())
                        || "Catan Summer Season".equalsIgnoreCase(season.getName());

                int roundsToCreate = 1 + (int) (season.getSeasonId() % 3); // yields 1..3 deterministically

                for (int r = 0; r < roundsToCreate; r++) {
                    RoundDao round = RoundDao.builder()
                            .roundDate(OffsetDateTime.now(ZoneId.of("America/New_York")).minusDays(r + 1))
                            .roundResults(new ArrayList<>())
                            .creator(season.getCreator())
                            .build();

                    List<com.sgg.users.UserDao> participants = new ArrayList<>();
                    participants.add(user1);
                    participants.add(user2);
                    if (includeUser3 && user3 != null) participants.add(user3);

                    for (int i = 0; i < participants.size(); i++) {
                        RoundResultDao rr = RoundResultDao.builder()
                                .place(i + 1)
                                .points(0.0)
                                .user(participants.get(i))
                                .build();
                        rr.setRound(round);
                        round.getRoundResults().add(rr);
                    }

                    season.addRound(round);
                }

                seasonRepository.update(season);
                log.info("created {} rounds for season {}", roundsToCreate, season.getName());
            }
        } catch (Exception e) {
            log.error("failed to create test rounds: {}", e.getMessage(), e);
        }
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

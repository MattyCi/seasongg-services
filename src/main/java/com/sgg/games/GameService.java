package com.sgg.games;

import com.sgg.common.exception.ClientException;
import com.sgg.games.model.GameDto;
import com.sgg.games.model.GameMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Singleton
@Slf4j
@AllArgsConstructor(onConstructor_ = @Inject)
public class GameService {

    private GameRepository gameRepository;
    private GameMapper gameMapper;
    private ExternalGameClient gameClient;

    public GameDto maybeCreateGame(GameDto game) {
        val storedGame = gameRepository.findById(game.getGameId());
        if (storedGame.isPresent()) {
            return gameMapper.toGameDto(storedGame.get());
        } else {
            val externalGame = gameClient.getGame(game.getGameId())
                    .blockOptional(Duration.ofSeconds(10)); // TODO: eventually make everything reactive
            if (externalGame.isPresent()) {
                val gameDao = gameMapper.toGameDao(externalGame.get());
                gameDao.setDateAdded(OffsetDateTime.now(ZoneId.of("America/New_York")));
                gameRepository.save(gameDao);
                return externalGame.get();
            } else {
                throw new ClientException("Cannot create game that doesn't exist in external service.");
            }
        }
    }
}

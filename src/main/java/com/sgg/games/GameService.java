package com.sgg.games;

import com.sgg.games.model.GameDao;
import com.sgg.games.model.GameDto;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
@AllArgsConstructor(onConstructor_ = @Inject)
public class GameService {

    private final GameRepository gameRepository;

    public GameDto createGame(GameDto game) {
        // TODO: add validation for game creation - use bgg api to validate?
        gameRepository.save(new GameDao(game.getGameId(), game.getName(), null));
        return game;
    }
}

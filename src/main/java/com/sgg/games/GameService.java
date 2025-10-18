package com.sgg.games;

import com.sgg.games.model.GameDto;
import com.sgg.games.model.GameMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
@AllArgsConstructor(onConstructor_ = @Inject)
public class GameService {

    private GameRepository gameRepository;
    private GameMapper gameMapper;

    // TODO: add validation for game creation - use bgg api to validate?
    public GameDto maybeCreateGame(GameDto game) {
        if (gameRepository.findById(game.getGameId()).isEmpty())
            gameRepository.save(gameMapper.toGameDao(game));
        return game;
    }
}

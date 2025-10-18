package com.sgg.games

import com.sgg.games.model.GameDao
import com.sgg.games.model.GameDto
import com.sgg.games.model.GameMapper
import spock.lang.Specification

class GameServiceSpec extends Specification {
    GameRepository gameRepository = Mock()
    GameMapper gameMapper = Mock()

    GameService gameService = new GameService(gameRepository, gameMapper)

    def "should create game when it does not exist"() {
        given:
        def gameDto = new GameDto(gameId: 123, name: "Catan")
        def gameDao = new GameDao(gameId: 123, name: "Catan")

        when:
        def result = gameService.maybeCreateGame(gameDto)

        then:
        1 * gameRepository.findById(123) >> Optional.empty()
        1 * gameMapper.toGameDao(gameDto) >> gameDao
        1 * gameRepository.save(gameDao)
        0 * _
        result == gameDto
    }

    def "should not create game when it already exists"() {
        given:
        def gameDto = new GameDto(gameId: 123, name: "Catan")
        def gameDao = new GameDao(gameId: 123, name: "Catan")

        when:
        def result = gameService.maybeCreateGame(gameDto)

        then:
        1 * gameRepository.findById(123) >> Optional.of(gameDao)
        0 * _
        result == gameDto
    }
}

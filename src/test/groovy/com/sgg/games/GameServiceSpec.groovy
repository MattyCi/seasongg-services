package com.sgg.games

import com.sgg.common.exception.ClientException
import com.sgg.games.model.GameDao
import com.sgg.games.model.GameDto
import com.sgg.games.model.GameMapper
import reactor.core.publisher.Mono
import spock.lang.Specification

class GameServiceSpec extends Specification {
    GameRepository gameRepository = Mock()
    GameMapper gameMapper = Mock()
    ExternalGameClient gameClient = Mock()

    GameService gameService = new GameService(gameRepository, gameMapper, gameClient)

    def "should create game if not already stored"() {
        given:
        def gameDto = new GameDto(gameId: 123, name: "Catan")
        def gameDao = new GameDao(gameId: 123, name: "Catan")

        when:
        def result = gameService.maybeCreateGame(gameDto)

        then:
        1 * gameRepository.findById(123) >> Optional.empty()

        and: "the game exists in BGG"
        1 * gameClient.getGame(123) >> Mono.just(gameDto)

        and:
        1 * gameMapper.toGameDao(gameDto) >> gameDao
        1 * gameRepository.save(gameDao)
        0 * _
        result == gameDto
    }

    def "should not create game if not found in BGG"() {
        given:
        def gameDto = new GameDto(gameId: 123, name: "Catan")

        when:
        gameService.maybeCreateGame(gameDto)

        then:
        1 * gameRepository.findById(123) >> Optional.empty()
        1 * gameClient.getGame(123) >> Mono.empty()
        0 * _

        def e = thrown(ClientException)
        e.message == "Cannot create game that doesn't exist in external service."
    }

    def "should not create game when it already exists"() {
        given:
        def gameDto = new GameDto(gameId: 123, name: "Catan")
        def gameDao = new GameDao(gameId: 123, name: "Catan")

        when:
        def result = gameService.maybeCreateGame(gameDto)

        then:
        1 * gameRepository.findById(123) >> Optional.of(gameDao)
        1 * gameMapper.toGameDto(gameDao) >> gameDto
        0 * _
        result == gameDto
    }

    def "should not create game with incorrect details"() {
        given:
        def badInputGameDto = new GameDto(gameId: 123, name: "garbage input") // ID mismatch with BGG game
        def bggGameDto = new GameDto(gameId: 123, name: "Catan")
        def gameDao = new GameDao(gameId: 123, name: "Catan")

        when:
        def result = gameService.maybeCreateGame(badInputGameDto)

        then:
        1 * gameRepository.findById(123) >> Optional.empty()

        and: "the game exists in BGG"
        1 * gameClient.getGame(123) >> Mono.just(bggGameDto)

        and:
        1 * gameMapper.toGameDao(bggGameDto) >> gameDao
        1 * gameRepository.save({ GameDao g ->
            assert g.name == "Catan" // correct name is persisted
        })
        0 * _
        result == bggGameDto
    }
}

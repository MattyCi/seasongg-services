package com.sgg.games

import com.sgg.common.exception.ClientException
import com.sgg.common.exception.SggException
import com.sgg.games.model.GameDao
import com.sgg.games.model.GameDto
import com.sgg.games.model.GameMapper
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

class GameServiceSpec extends Specification {
    GameRepository gameRepository = Mock()
    GameMapper gameMapper = Mock()
    ExternalGameClient gameClient = Mock(constructorArgs: [null, null, null])

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

    def "should set date added when creating a game"() {
        given:
        def gameDto = new GameDto(gameId: 123, name: "Catan")
        def gameDao = new GameDao(gameId: 123, name: "Catan")
        gameRepository.findById(123) >> Optional.empty()
        gameClient.getGame(123) >> Mono.just(gameDto)
        gameMapper.toGameDao(gameDto) >> gameDao

        when:
        gameService.maybeCreateGame(gameDto)

        then:
        1 * gameRepository.save({ GameDao g ->
            assert g.dateAdded != null
        })
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

    def "should persist BGG details rather than the caller-supplied details"() {
        given:
        def badInputGameDto = new GameDto(gameId: 123, name: "garbage input")
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

    def "do not save game if external client fails"() {
        given:
        def gameDto = new GameDto(gameId: 123, name: "Catan")

        when:
        gameService.maybeCreateGame(gameDto)

        then:
        1 * gameRepository.findById(123) >> Optional.empty()
        1 * gameClient.getGame(123) >> Mono.error(new SggException("Unexpected bgg error."))
        0 * gameMapper._
        0 * gameRepository.save(_)

        def e = thrown(SggException)
        e.message == "Unexpected bgg error."
    }

    def "get popular games delegates to external client"() {
        given:
        def games = [new GameDto(gameId: 1L, rank: 1L, name: "Game 1")]

        when:
        def result = gameService.getPopularGames()

        then:
        1 * gameClient.getPopularGames() >> Mono.just(games)
        0 * _

        and:
        StepVerifier.create(result).expectNext(games).verifyComplete()
    }

    def "search games delegates to external client"() {
        given:
        def games = [new GameDto(gameId: 13L, name: "Catan")]

        when:
        def result = gameService.searchGames("catan")

        then:
        1 * gameClient.searchGames("catan") >> Mono.just(games)
        0 * _

        and:
        StepVerifier.create(result).expectNext(games).verifyComplete()
    }
}
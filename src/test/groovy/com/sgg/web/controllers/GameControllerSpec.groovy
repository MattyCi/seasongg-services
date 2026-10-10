package com.sgg.web.controllers

import com.sgg.games.GameService
import com.sgg.games.model.GameDto
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpStatus
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.validation.ConstraintViolationException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

@MicronautTest(startApplication = false)
@Property(name = "games.bgg.token", value = "test-token")
class GameControllerSpec extends Specification {

    static final String PATTERN_MESSAGE = "Invalid characters provided in search query."

    @Inject
    GameController controller

    GameService gameService = Mock(constructorArgs: [null, null, null])

    @MockBean(GameService)
    GameService gameService() {
        gameService
    }

    def "get popular returns 200 with the service result"() {
        given:
        def games = [new GameDto(gameId: 1L, rank: 1L, name: "Game 1")]

        when:
        def response = controller.getPopularGames()

        then:
        1 * gameService.getPopularGames() >> Mono.just(games)
        0 * _

        and:
        response.status() == HttpStatus.OK
        StepVerifier.create(response.body()).expectNext(games).verifyComplete()
    }

    def "search games returns 200 with the service result"() {
        given:
        def games = [new GameDto(gameId: 13L, name: "Catan")]

        when:
        def response = controller.searchGames("catan")

        then:
        1 * gameService.searchGames("catan") >> Mono.just(games)
        0 * _

        and:
        response.status() == HttpStatus.OK
        StepVerifier.create(response.body()).expectNext(games).verifyComplete()
    }

    def "search games passes the query through unchanged (normalization happens downstream)"() {
        when:
        controller.searchGames("  star   wars ")

        then:
        1 * gameService.searchGames("  star   wars ") >> Mono.just([])
    }

    def "search games accepts #query"() {
        when:
        controller.searchGames(query)

        then:
        1 * gameService.searchGames(query) >> Mono.just([])

        where:
        query << [
                "Catan",
                "Ticket to Ride",
                "7 Wonders",
                "Dr. Eureka",
                "Pandemic: Legacy",
                "Rock & Roll",
                "Don't Panic",
                "Don\u2019t Panic",
                "Caf\u00e9 International",
                "\u30dc\u30fc\u30c9\u30b2\u30fc\u30e0",
                "Hello, World!",
                "Who? What?",
                "Cash \$ Flow",
                "Mage Knight (Ultimate)",
                "Twilight Struggle 1/2",
                "Spirit-Island",
                "42",
                "ab",
                "a" * 25
        ]
    }

    def "search games rejects #description with the pattern message"() {
        when:
        controller.searchGames(query)

        then:
        def e = thrown(ConstraintViolationException)
        e.constraintViolations*.message.contains(PATTERN_MESSAGE)
        0 * gameService._

        where:
        description        | query
        "script tags"      | "<script>alert(1)</script>"
        "semicolons"       | "catan; drop table games"
        "percent signs"    | "100%"
        "asterisks"        | "cat*"
        "equals signs"     | "a=b"
        "newlines"         | "cata\nn"
        "tabs"             | "cata\tn"
        "punctuation only" | "!?!?"
    }

    def "search games rejects #description"() {
        when:
        controller.searchGames(query)

        then:
        thrown(ConstraintViolationException)
        0 * gameService._

        where:
        description               | query
        "an empty query"          | ""
        "a whitespace-only query" | "   "
        "a single character"      | "a"
        "a query over 25 chars"   | "a" * 26
    }
}
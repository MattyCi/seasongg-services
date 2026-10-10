package com.sgg.games

import com.sgg.common.exception.NotFoundException
import com.sgg.common.exception.SggException
import com.sgg.games.bgg.BggGameXmlParser
import com.sgg.games.bgg.BggRequestBuilder
import com.sgg.games.model.GameDto
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

class ExternalGameClientSpec extends Specification {

    static final String RAW_RESPONSE = "<items/>"
    static final String GET_GAME_ERROR = "Unexpected error occurred finding game from BGG."

    HttpClient httpClient = Mock()
    BggRequestBuilder requestBuilder = Mock()
    BggGameXmlParser xmlParser = Mock()

    ExternalGameClient client = new ExternalGameClient(httpClient, requestBuilder, xmlParser)

    HttpRequest<?> request = HttpRequest.GET("https://example.com/bgg")

    def "get game succeeds"() {
        given:
        def game = new GameDto(gameId: 12345L, name: "Catan")
        requestBuilder.gameById(12345L) >> request
        httpClient.retrieve(request, String) >> Mono.just(RAW_RESPONSE)

        when:
        StepVerifier.create(client.getGame(12345L))
                .expectNext(game)
                .verifyComplete()

        then:
        1 * xmlParser.parseSingleGame(RAW_RESPONSE) >> game
    }

    def "get game handles game not found"() {
        given:
        requestBuilder.gameById(99999L) >> request
        httpClient.retrieve(request, String) >> Mono.just(RAW_RESPONSE)

        when:
        StepVerifier.create(client.getGame(99999L))
                .expectNextCount(0)
                .verifyComplete()

        then:
        1 * xmlParser.parseSingleGame(RAW_RESPONSE) >> { throw new NotFoundException("Game not found.") }
    }

    def "get game handles errors while parsing xml response from bgg"() {
        given:
        requestBuilder.gameById(12345L) >> request
        httpClient.retrieve(request, String) >> Mono.just(RAW_RESPONSE)

        when:
        StepVerifier.create(client.getGame(12345L))
                .expectErrorMatches { it instanceof SggException && it.message == GET_GAME_ERROR }
                .verify()

        then:
        1 * xmlParser.parseSingleGame(RAW_RESPONSE) >> { throw new SggException("Error parsing response from BGG.") }
    }

    def "get game handles unexpected http errors from bgg"() {
        given:
        requestBuilder.gameById(12345L) >> request
        httpClient.retrieve(request, String) >>
                Mono.error(new HttpClientResponseException("Error", HttpResponse.serverError()))

        when:
        StepVerifier.create(client.getGame(12345L))
                .expectErrorMatches { it instanceof SggException && it.message == GET_GAME_ERROR }
                .verify()

        then:
        0 * xmlParser._
    }

    def "get popular games succeeds"() {
        given:
        def games = [new GameDto(gameId: 1L, rank: 1L, name: "Game 1"),
                     new GameDto(gameId: 2L, rank: 2L, name: "Game 2")]
        requestBuilder.popularGames() >> request
        httpClient.retrieve(request, String) >> Mono.just(RAW_RESPONSE)

        when:
        StepVerifier.create(client.getPopularGames())
                .expectNext(games)
                .verifyComplete()

        then:
        1 * xmlParser.parsePopularGames(RAW_RESPONSE) >> games
    }

    def "get popular games returns an empty list when parsing fails"() {
        given:
        requestBuilder.popularGames() >> request
        httpClient.retrieve(request, String) >> Mono.just(RAW_RESPONSE)

        when:
        StepVerifier.create(client.getPopularGames())
                .expectNext([])
                .verifyComplete()

        then:
        1 * xmlParser.parsePopularGames(RAW_RESPONSE) >> { throw new SggException("Response from BGG was empty.") }
    }

    def "get popular games returns an empty list when the HTTP call fails"() {
        given:
        requestBuilder.popularGames() >> request
        httpClient.retrieve(request, String) >>
                Mono.error(new HttpClientResponseException("Error", HttpResponse.serverError()))

        when:
        StepVerifier.create(client.getPopularGames())
                .expectNext([])
                .verifyComplete()

        then:
        0 * xmlParser._
    }

    def "search games succeeds"() {
        given:
        def games = [new GameDto(gameId: 13L, name: "Catan")]
        requestBuilder.searchGames("catan") >> request
        httpClient.retrieve(request, String) >> Mono.just(RAW_RESPONSE)

        when:
        StepVerifier.create(client.searchGames("catan"))
                .expectNext(games)
                .verifyComplete()

        then:
        1 * xmlParser.parseSearchResults(RAW_RESPONSE) >> games
    }

    def "search games trims and collapses repeated spaces before building the request"() {
        when:
        StepVerifier.create(client.searchGames(input))
                .expectNext([])
                .verifyComplete()

        then:
        1 * requestBuilder.searchGames(expected) >> request
        1 * httpClient.retrieve(request, String) >> Mono.just(RAW_RESPONSE)
        1 * xmlParser.parseSearchResults(RAW_RESPONSE) >> []

        where:
        input                 || expected
        "catan"               || "catan"
        "  catan  "           || "catan"
        "ticket   to    ride" || "ticket to ride"
        "  star   wars  "     || "star wars"
    }

    def "search games returns an empty list when parsing fails"() {
        given:
        requestBuilder.searchGames("catan") >> request
        httpClient.retrieve(request, String) >> Mono.just(RAW_RESPONSE)

        when:
        StepVerifier.create(client.searchGames("catan"))
                .expectNext([])
                .verifyComplete()

        then:
        1 * xmlParser.parseSearchResults(RAW_RESPONSE) >> { throw new SggException("some error") }
    }

    def "search games returns an empty list when the HTTP call fails"() {
        given:
        requestBuilder.searchGames("catan") >> request
        httpClient.retrieve(request, String) >>
                Mono.error(new HttpClientResponseException("Error", HttpResponse.serverError()))

        when:
        StepVerifier.create(client.searchGames("catan"))
                .expectNext([])
                .verifyComplete()

        then:
        0 * xmlParser._
    }
}
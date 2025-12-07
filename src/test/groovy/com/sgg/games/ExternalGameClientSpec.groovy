package com.sgg.games


import com.sgg.common.exception.SggException
import com.sgg.games.model.GameDto
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

class ExternalGameClientSpec extends Specification {

    HttpClient httpClient = Mock(HttpClient)
    ExternalGameClient externalGameClient = new ExternalGameClient(httpClient)

    def "getPopularGames returns list of games when response is valid"() {
        given:
        String validXmlResponse = """
        <items>
            <item id="1" rank="1">
                <name value="Game 1"/>
                <yearpublished value="2020"/>
                <thumbnail value="https://example.com/game1.png"/>
            </item>
            <item id="2" rank="2">
                <name value="Game 2"/>
                <yearpublished value="2022"/>
                <thumbnail value="https://example.com/game2.png"/>
            </item>
        </items>
        """
        httpClient.retrieve(_ as HttpRequest, String.class) >> Mono.just(validXmlResponse)

        when:
        def result = externalGameClient.getPopularGames().block()

        then:
        result.size() == 2
        result[0].gameId == 1
        result[0].rank == 1
        result[0].name == "Game 1"
        result[0].yearPublished == "2020"
        result[0].thumbnail == "https://example.com/game1.png"

        and:
        result[1].gameId == 2
        result[1].rank == 2
        result[1].name == "Game 2"
        result[1].yearPublished == "2022"
        result[1].thumbnail == "https://example.com/game2.png"
    }

    def "getPopularGames returns empty list when response is empty"() {
        given:
        httpClient.retrieve(_ as HttpRequest, String.class) >> Mono.just("")

        when:
        def result = externalGameClient.getPopularGames().block()

        then:
        result.isEmpty()
    }

    def "getPopularGames handles invalid XML gracefully"() {
        given:
        String invalidXmlResponse = "<invalid>"
        httpClient.retrieve(_ as HttpRequest, String.class) >> Mono.just(invalidXmlResponse)

        when:
        def result = externalGameClient.getPopularGames().block()

        then:
        result.isEmpty()
    }

    def "getPopularGames handles HTTP errors gracefully"() {
        given:
        httpClient.retrieve(_ as HttpRequest, String.class) >> Mono.error {
            throw new HttpClientResponseException("Error", HttpResponse.serverError())
        }

        when:
        def result = externalGameClient.getPopularGames().block()

        then:
        result.isEmpty()
    }

    def "getGame returns game details when response is valid"() {
        given:
        def expectedGame = new GameDto(
                gameId: 12345,
                name: "Primary Name",
                yearPublished: 2023,
                thumbnail: "https://example.com/game.png"
        )
        def validXmlResponse = """
        <items>
            <item type="boardgame" id="12345">
                <name type="primary" sortindex="1" value="Primary Name" />
                <name type="alternate" sortindex="1" value="Secondary Name 1" />
                <name type="alternate" sortindex="1" value="Secondary Name 2" />
                <yearpublished value="2023"/>
                <thumbnail>https://example.com/game.png</thumbnail>
            </item>
        </items>
        """
        httpClient.retrieve(_ as HttpRequest) >> Mono.just(validXmlResponse)

        when:
        def result = externalGameClient.getGame(12345L)

        then:
        StepVerifier.create(result)
                .expectNext(expectedGame)
                .expectComplete()
                .verify()
    }

    def "getGame throws NotFoundException when game is not found"() {
        given:
        def emptyXmlResponse = "<items></items>"
        httpClient.retrieve(_ as HttpRequest) >> Mono.just(emptyXmlResponse)

        when:
        def result = externalGameClient.getGame(99999L)

        then:
        StepVerifier.create(result)
            .expectNextCount(0)
            .verifyComplete()
    }

    def "getGame handles error XML gracefully"() {
        given:
        String invalidXmlResponse = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
                <item>
                    <error message='Invalid Item'/>
                </item>
            </items>
        """
        httpClient.retrieve(_ as HttpRequest) >> Mono.just(invalidXmlResponse)

        when:
        def result = externalGameClient.getGame(12345L)

        then:
        StepVerifier.create(result)
                .expectErrorSatisfies { e ->
                    assert e instanceof SggException
                    assert e.getMessage() == "Unexpected error occurred finding game from BGG."
                }.verify()
    }

    def "getGame handles invalid XML gracefully"() {
        given:
        String invalidXmlResponse = "<invalid>"
        httpClient.retrieve(_ as HttpRequest) >> Mono.just(invalidXmlResponse)

        when:
        def result = externalGameClient.getGame(12345L)

        then:
        StepVerifier.create(result)
            .expectErrorSatisfies { e ->
                assert e instanceof SggException
                assert e.getMessage() == "Unexpected error occurred finding game from BGG."
            }.verify()
    }

    def "getGame handles HTTP errors gracefully"() {
        given:
        httpClient.retrieve(_ as HttpRequest) >> Mono.error {
            throw new HttpClientResponseException("Error", HttpResponse.serverError())
        }

        when:
        def result = externalGameClient.getGame(12345L)

        then:
        StepVerifier.create(result)
            .expectErrorSatisfies { e ->
                assert e instanceof SggException
                assert e.getMessage() == "Unexpected error occurred finding game from BGG."
            }.verify()
    }
}

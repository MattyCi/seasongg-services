package com.sgg.games

import com.sgg.common.exception.NotFoundException
import com.sgg.common.exception.SggException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import reactor.core.publisher.Mono
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
        String validXmlResponse = """
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
        def result = externalGameClient.getGame(12345L).block()

        then:
        result.gameId == 12345L
        result.name == "Primary Name"
        result.yearPublished == "2023"
        result.thumbnail == "https://example.com/game.png"
    }

    def "getGame throws NotFoundException when game is not found"() {
        given:
        String emptyXmlResponse = "<items></items>"
        httpClient.retrieve(_ as HttpRequest) >> Mono.just(emptyXmlResponse)

        when:
        externalGameClient.getGame(99999L).block()

        then:
        thrown(NotFoundException)
    }

    def "getGame handles invalid XML gracefully"() {
        given:
        String invalidXmlResponse = "<invalid>"
        httpClient.retrieve(_ as HttpRequest) >> Mono.just(invalidXmlResponse)

        when:
        externalGameClient.getGame(12345L).block()

        then:
        def e = thrown(SggException)
        e.message == "Unexpected error occurred trying to find game from external service."
    }

    def "getGame handles HTTP errors gracefully"() {
        given:
        httpClient.retrieve(_ as HttpRequest) >> Mono.error {
            throw new HttpClientResponseException("Error", HttpResponse.serverError())
        }

        when:
        externalGameClient.getGame(12345L).block()

        then:
        def e = thrown(SggException)
        e.getMessage() == "Unexpected error occurred trying to find game from external service."
    }
}

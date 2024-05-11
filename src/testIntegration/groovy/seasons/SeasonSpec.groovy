package seasons

import com.sgg.common.exception.SggError
import com.sgg.games.model.GameDto
import com.sgg.seasons.SeasonRepository
import com.sgg.seasons.model.SeasonDto
import com.sgg.seasons.model.SeasonStatus
import common.AbstractSpecification
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.OffsetDateTime

@MicronautTest
class SeasonSpec extends AbstractSpecification {

    @Inject
    private SeasonRepository seasonRepository

    def "should create season"() {
        given:
        def season = new SeasonDto(
                name: "season-name",
                endDate: OffsetDateTime.parse("3000-04-17T22:00:00-05:00"),
                game: new GameDto(
                        gameId: 123,
                        name: "Catan"
                )
        )
        def request = HttpRequest.POST('/v1/seasons', season)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)

        when:
        def rsp = client.toBlocking().exchange(request, SeasonDto)

        then:
        rsp.status == HttpStatus.OK
        seasonRepository.count() == 1
        rsp.getBody().isPresent()
        rsp.getBody().get().status == SeasonStatus.ACTIVE
    }

    def "should not create invalid season"() {
        given:
        def season = new SeasonDto(
                name: "@#\$#",
                endDate: null,
                game: new GameDto(
                        gameId: 123,
                        name: "Catan"
                )
        )
        def request = HttpRequest.POST('/v1/seasons', season)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)

        when:
        client.toBlocking().exchange(request, SeasonDto)

        then:
        def ex = thrown(HttpClientResponseException)
        assert ex.getStatus() == HttpStatus.BAD_REQUEST
        assert ex.response.getBody(SggError).get().errorMessage == "The season must have an end date. Invalid characters detected in the season name."
    }

    def "should prevent season creation if not logged in"() {
        given:
        def season = new SeasonDto(
                name: "season-name",
                endDate: OffsetDateTime.parse("3000-04-17T22:00:00-05:00"),
                game: new GameDto(
                        gameId: 123,
                        name: "Catan"
                )
        )
        def request = HttpRequest.POST('/v1/seasons', season)

        when:
        client.toBlocking().exchange(request, SeasonDto)

        then:
        def e = thrown(HttpClientResponseException)
        e.status == HttpStatus.UNAUTHORIZED
    }
}

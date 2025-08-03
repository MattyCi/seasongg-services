package seasons

import com.sgg.common.exception.SggError
import com.sgg.games.GameRepository
import com.sgg.games.model.GameDto
import com.sgg.seasons.SeasonRepository
import com.sgg.seasons.model.SeasonDto
import com.sgg.seasons.model.SeasonStatus
import com.sgg.users.authz.PermissionRepository
import com.sgg.users.authz.PermissionType
import com.sgg.users.authz.UserPermissionRepository
import common.AbstractSpecification
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.OffsetDateTime

@MicronautTest(transactional = false)
class SeasonSpec extends AbstractSpecification {

    @Inject
    private SeasonRepository seasonRepository

    @Inject
    private GameRepository gameRepository

    @Inject
    private PermissionRepository permissionRepository

    @Inject
    private UserPermissionRepository userPermissionRepository

    def cleanup() {
        seasonRepository.deleteAll()
        gameRepository.deleteAll()
    }

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
        def createdSeason = rsp.getBody().get()

        then:
        rsp.status == HttpStatus.OK
        seasonRepository.count() == 1
        createdSeason.name == season.name
        createdSeason.status == SeasonStatus.ACTIVE
        createdSeason.creator.username == "integ-user"

        and: "should create write permissions tied to the seasonId"
        def permission = permissionRepository.findAll().first()
        permission.permissionType == PermissionType.WRITE
        permission.resourceId == seasonRepository.findAll().first().seasonId

        and: "should tie the permission to the user who created the season"
        def userPermission = userPermissionRepository.findAll().first()
        userPermission.userDao.username == rsp.body().creator.username
        userPermission.permissionDao.permId == permission.permId

        cleanup:
        seasonRepository.deleteAll()
        userPermissionRepository.deleteAll()
        permissionRepository.deleteAll()
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

    def "should disallow duplicate season names"() {
        given:
        def validSeason = new SeasonDto(
                name: "same-name",
                endDate: OffsetDateTime.parse("3000-04-17T22:00:00-05:00"),
                game: new GameDto(
                        gameId: 456,
                        name: "valid-game"
                )
        )
        def invalidSeason = new SeasonDto(
                name: "same-name",
                endDate: OffsetDateTime.parse("3000-04-17T22:00:00-05:00"),
                game: new GameDto(
                        gameId: 789,
                        name: "should-rollback"
                ),
                rounds: "temp"
        )
        def validRequest = HttpRequest.POST('/v1/seasons', validSeason)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        def invalidRequest = HttpRequest.POST('/v1/seasons', invalidSeason)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)

        when: "persisting valid season"
        def rsp = client.toBlocking().exchange(validRequest, SeasonDto)

        then: "request is valid"
        rsp.status == HttpStatus.OK

        when: "creating season with duplicate name"
        client.toBlocking().exchange(invalidRequest, SeasonDto)

        then:
        def e = thrown(HttpClientResponseException)
        e.status == HttpStatus.BAD_REQUEST
        e.response.getBody(SggError).get().errorMessage == "A season with that name already exists."
    }

    def "should rollback entire transaction if an error occurs"() {
        given:
        def validSeason = new SeasonDto(
                name: "same-name",
                endDate: OffsetDateTime.parse("3000-04-17T22:00:00-05:00"),
                game: new GameDto(
                        gameId: 456,
                        name: "valid-game"
                )
        )
        def invalidSeason = new SeasonDto(
                name: "same-name",
                endDate: OffsetDateTime.parse("3000-04-17T22:00:00-05:00"),
                game: new GameDto(
                        gameId: 789,
                        name: "should-rollback"
                ),
                rounds: "temp"
        )
        def validRequest = HttpRequest.POST('/v1/seasons', validSeason)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        def invalidRequest = HttpRequest.POST('/v1/seasons', invalidSeason)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)

        when: "persisting valid season"
        def rsp = client.toBlocking().exchange(validRequest, SeasonDto)

        then: "request is valid"
        rsp.status == HttpStatus.OK

        when: "creating season with rounds"
        client.toBlocking().exchange(invalidRequest, SeasonDto)

        then: "the second game should not have been added (the transaction should have rolled back)"
        def e = thrown(HttpClientResponseException)
        e.status == HttpStatus.BAD_REQUEST
        gameRepository.count() == 1
        e.response.getBody(SggError).get().errorMessage == "A season with that name already exists."
    }
}

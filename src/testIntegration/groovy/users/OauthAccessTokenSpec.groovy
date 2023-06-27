package users

import com.sgg.users.RefreshTokenRepository
import com.sgg.users.UserRegistrationRequest
import com.sgg.users.UserService
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.jwt.endpoints.TokenRefreshRequest
import io.micronaut.security.token.jwt.render.AccessRefreshToken
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@MicronautTest(rollback = false)
class OauthAccessTokenSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient client

    @Shared
    @Inject
    RefreshTokenRepository refreshTokenRepository

    @Shared
    @Inject
    UserService userService

    def setupSpec() {
        userService.registerUser(
                new UserRegistrationRequest(
                        "sgg-user",
                        "test123",
                        "test123"
                )
        )
    }

    void "Verify JWT access token refresh works"() {
        given:
        def creds = new UsernamePasswordCredentials("sgg-user", "test123")

        when: 'login endpoint is called with valid credentials'
        HttpRequest request = HttpRequest.POST('/login', creds)
        BearerAccessRefreshToken rsp = client.toBlocking().retrieve(request, BearerAccessRefreshToken)

        then: 'the refresh token is saved to the database'
        new PollingConditions().eventually {
            assert refreshTokenRepository.count() == old(refreshTokenRepository.count()) + 1
        }

        and: 'response contains an access token token'
        rsp.accessToken

        and: 'response contains a refresh token'
        rsp.refreshToken

        when:
        sleep(1_000) // sleep for one second to give time for the issued at `iat` Claim to change
        AccessRefreshToken refreshResponse = client.toBlocking().retrieve(HttpRequest.POST('/oauth/access_token',
                new TokenRefreshRequest(rsp.refreshToken)), AccessRefreshToken)

        then:
        refreshResponse.accessToken
        refreshResponse.accessToken != rsp.accessToken

        cleanup:
        refreshTokenRepository.deleteAll()
    }

}
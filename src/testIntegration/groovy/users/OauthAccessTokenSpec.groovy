package users


import com.sgg.users.UserRegistrationRequest
import com.sgg.users.UserService
import com.sgg.users.security.RefreshTokenService
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.endpoints.TokenRefreshRequest
import io.micronaut.security.token.render.AccessRefreshToken
import io.micronaut.security.token.render.BearerAccessRefreshToken
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@MicronautTest(transactional = false)
class OauthAccessTokenSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient client

    @Shared
    @Inject
    RefreshTokenService refreshTokenService

    @Shared
    @Inject
    UserService userService

    def setupSpec() {
        userService.registerUser(
                new UserRegistrationRequest(
                        "oauth-user",
                        "test123",
                        "test123"
                )
        )
    }

    void "verify JWT access token refresh works"() {
        given:
        def creds = new UsernamePasswordCredentials("oauth-user", "test123")

        when: 'login endpoint is called with valid credentials'
        HttpRequest request = HttpRequest.POST('/login', creds)
        BearerAccessRefreshToken rsp = client.toBlocking().retrieve(request, BearerAccessRefreshToken)

        then: 'the refresh token is saved to the database'
        new PollingConditions().eventually {
            assert refreshTokenService.count() == old(refreshTokenService.count()) + 1
        }

        and: 'response contains an access token token'
        rsp.accessToken

        and: 'response contains a refresh token'
        rsp.refreshToken

        when:
        sleep(1_000) // sleep for one second to give time for the issued at `iat` Claim to change
        AccessRefreshToken refreshResponse = client.toBlocking().retrieve(HttpRequest.POST('/oauth/access_token',
                new TokenRefreshRequest(TokenRefreshRequest.GRANT_TYPE_REFRESH_TOKEN, rsp.refreshToken)), AccessRefreshToken)

        then:
        refreshResponse.accessToken
        refreshResponse.accessToken != rsp.accessToken

        cleanup:
        refreshTokenService.deleteAll()
        userService.deleteUser("oauth-user")
    }

}
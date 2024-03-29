package users

import com.sgg.users.UserRegistrationRequest
import com.sgg.users.UserService
import com.sgg.users.security.RefreshTokenService
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.endpoints.TokenRefreshRequest
import io.micronaut.security.token.render.BearerAccessRefreshToken
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import static io.micronaut.http.HttpStatus.BAD_REQUEST

@MicronautTest(transactional = false)
class RefreshTokenRevokedSpec extends Specification {

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
                        "revoke-user",
                        "test123",
                        "test123"
                )
        )
    }

    void 'trying to refresh a revoked refresh token returns error'() {
        given:
        final loginRequest = HttpRequest.POST('/login',
                new UsernamePasswordCredentials("revoke-user", "test123"))

        when: "a user logs in"
        final rsp = client.toBlocking().retrieve(loginRequest, BearerAccessRefreshToken)

        then: "a refresh token is returned"
        rsp.refreshToken

        and: "a refresh token is persisted"
        new PollingConditions().eventually {
            assert refreshTokenService.count() == old(refreshTokenService.count()) + 1
        }

        when: "a user tries to refresh their revoked refresh token"
        refreshTokenService.revokeAll()
        final refreshTokenRequest = HttpRequest.POST('/oauth/access_token',
                new TokenRefreshRequest(TokenRefreshRequest.GRANT_TYPE_REFRESH_TOKEN, rsp.refreshToken))
        client.toBlocking().retrieve(refreshTokenRequest, BearerAccessRefreshToken)

        then:
        HttpClientResponseException e = thrown()
        e.status == BAD_REQUEST

        when:
        Optional<Map> mapOptional = e.response.getBody(Map)

        then:
        mapOptional.isPresent()

        when:
        Map m = mapOptional.get()

        then:
        m.error == 'invalid_grant'
        m.error_description == 'refresh token revoked'

        cleanup:
        refreshTokenService.deleteAll()
        userService.deleteUser("revoke-user")
    }

}
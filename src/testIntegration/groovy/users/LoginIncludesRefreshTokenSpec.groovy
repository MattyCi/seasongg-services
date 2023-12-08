package users

import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import com.sgg.users.UserRegistrationRequest
import com.sgg.users.UserService
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.render.BearerAccessRefreshToken
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Shared
import spock.lang.Specification

@MicronautTest
class LoginIncludesRefreshTokenSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient client

    @Shared
    @Inject
    UserService userService

    def setupSpec() {
        userService.registerUser(
                new UserRegistrationRequest(
                        "refresh-token-user",
                        "test123",
                        "test123"
                )
        )
    }

    void "upon successful authentication, the user gets an access token and a refresh token"() {
        when: 'login endpoint is called with valid credentials'
        UsernamePasswordCredentials creds =
                new UsernamePasswordCredentials("refresh-token-user", "test123")
        HttpRequest request = HttpRequest.POST('/login', creds)
        BearerAccessRefreshToken rsp = client.toBlocking().retrieve(request, BearerAccessRefreshToken)

        then:
        rsp.username == 'refresh-token-user'
        rsp.accessToken
        rsp.refreshToken

        and: 'access token is a JWT'
        JWTParser.parse(rsp.accessToken) instanceof SignedJWT

        cleanup:
        userService.deleteUser("refresh-token-user")
    }

}
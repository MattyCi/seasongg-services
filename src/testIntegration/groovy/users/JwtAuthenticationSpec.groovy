package users

import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import com.sgg.users.UserRegistrationRequest
import com.sgg.users.UserService
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.render.BearerAccessRefreshToken
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Shared
import spock.lang.Specification

import static io.micronaut.http.HttpStatus.OK
import static io.micronaut.http.HttpStatus.UNAUTHORIZED
import static io.micronaut.http.MediaType.TEXT_PLAIN

@MicronautTest
class JwtAuthenticationSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient client

    @Shared
    @Inject
    UserService userService

    def setupSpec() {
        userService.registerUser(
                new UserRegistrationRequest(
                        "jwt-user",
                        "test123",
                        "test123"
                )
        )
    }

    void 'accessing a secured URL without authenticating returns unauthorized'() {
        when:
        client.toBlocking().exchange(HttpRequest.GET('/').accept(TEXT_PLAIN))

        then:
        HttpClientResponseException e = thrown()
        e.status == UNAUTHORIZED
    }

    void "upon successful authentication, a JSON Web token is issued to the user"() {
        when: 'login endpoint is called with valid credentials'
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials("jwt-user", "test123")
        HttpRequest request = HttpRequest.POST('/login', creds)
        HttpResponse<BearerAccessRefreshToken> rsp = client.toBlocking().exchange(request, BearerAccessRefreshToken)

        then: 'the endpoint can be accessed'
        rsp.status == OK

        when:
        BearerAccessRefreshToken bearerAccessRefreshToken = rsp.body()

        then:
        bearerAccessRefreshToken.username == 'jwt-user'
        bearerAccessRefreshToken.accessToken

        and: 'the access token is a signed JWT'
        JWTParser.parse(bearerAccessRefreshToken.accessToken) instanceof SignedJWT

        when: 'passing the access token in the Authorization HTTP Header allows the user to access a secured endpoint'
        String accessToken = bearerAccessRefreshToken.accessToken
        HttpRequest requestWithAuthorization = HttpRequest.GET('/test' ) // TODO: change to an actual protected endpoint
                .accept(TEXT_PLAIN)
                .bearerAuth(accessToken)
        HttpResponse<String> response = client.toBlocking().exchange(requestWithAuthorization, String)

        then:
        response.status == OK
        response.body() == 'jwt-user'

        cleanup:
        userService.deleteUser("jwt-user")
    }

}
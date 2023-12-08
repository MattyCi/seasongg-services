package users

import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.endpoints.TokenRefreshRequest
import io.micronaut.security.token.generator.RefreshTokenGenerator
import io.micronaut.security.token.render.BearerAccessRefreshToken
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import static io.micronaut.http.HttpStatus.BAD_REQUEST

@MicronautTest
class RefreshTokenNotFoundSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient client

    @Inject
    RefreshTokenGenerator refreshTokenGenerator

    void 'trying to request an access token'() {
        given:
        Authentication user = Authentication.build("sherlock")

        when:
        String refreshToken = refreshTokenGenerator.createKey(user)
        Optional<String> refreshTokenOptional = refreshTokenGenerator.generate(user, refreshToken)

        then:
        refreshTokenOptional.isPresent()

        when: "a signed refresh token that was never persisted is supplied"
        String signedRefreshToken = refreshTokenOptional.get()
        Argument<BearerAccessRefreshToken> bodyArgument = Argument.of(BearerAccessRefreshToken)
        Argument<Map> errorArgument = Argument.of(Map)
        HttpRequest req = HttpRequest.POST("/oauth/access_token",
                new TokenRefreshRequest(TokenRefreshRequest.GRANT_TYPE_REFRESH_TOKEN, signedRefreshToken))
        client.toBlocking().exchange(req, bodyArgument, errorArgument)

        then: "an error is returned"
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
        m.error_description == 'refresh token not found'
    }

}
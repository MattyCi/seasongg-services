package users

import com.sgg.users.RefreshTokenDao
import com.sgg.users.UserDao
import com.sgg.users.UserRepository
import com.sgg.users.security.RefreshTokenService
import io.micronaut.context.ApplicationContext
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.token.generator.RefreshTokenGenerator
import io.micronaut.security.token.jwt.endpoints.TokenRefreshRequest
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import static io.micronaut.http.HttpStatus.BAD_REQUEST

class RefreshTokenRevokedSpec extends Specification {

    @AutoCleanup
    @Shared
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, [:])

    @Shared
    HttpClient client = embeddedServer.applicationContext.createBean(HttpClient, embeddedServer.URL)

    @Shared
    RefreshTokenGenerator refreshTokenGenerator = embeddedServer.applicationContext.getBean(RefreshTokenGenerator)

    @Shared
    RefreshTokenService refreshTokenService = embeddedServer.applicationContext.getBean(RefreshTokenService)

    @Shared
    UserRepository userRepository = embeddedServer.applicationContext.getBean(UserRepository)

    void 'Accessing a secured URL without authenticating returns unauthorized'() {
        given:
        Authentication user = Authentication.build("sherlock")

        when:
        String refreshToken = refreshTokenGenerator.createKey(user)
        Optional<String> refreshTokenOptional = refreshTokenGenerator.generate(user, refreshToken)

        then:
        refreshTokenOptional.isPresent()

        and:
        final userDao = UserDao.builder()
            .username("sgg-user")
            .password("test123")
            .build()

        userRepository.save(userDao)

        final refreshTokenDao = RefreshTokenDao.builder()
            .refreshToken(refreshToken)
            .userDao(userDao)
            .revoked(true)
            .build()

        when:
        String signedRefreshToken = refreshTokenOptional.get()
        refreshTokenService.persistRefreshToken(refreshTokenDao)

        then:
        refreshTokenService.count() == old(refreshTokenService.count()) + 1

        when:
        Argument<BearerAccessRefreshToken> bodyArgument = Argument.of(BearerAccessRefreshToken)
        Argument<Map> errorArgument = Argument.of(Map)
        client.toBlocking().exchange(
                HttpRequest.POST("/oauth/access_token", new TokenRefreshRequest(signedRefreshToken)),
                bodyArgument,
                errorArgument)

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
    }

    def cleanup() {
        refreshTokenService.deleteAll()
        userRepository.deleteAll()
    }

}
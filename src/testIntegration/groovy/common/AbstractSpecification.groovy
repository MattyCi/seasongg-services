package common

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.render.BearerAccessRefreshToken
import jakarta.inject.Inject
import spock.lang.Specification

import static io.micronaut.http.HttpStatus.OK

abstract class AbstractSpecification extends Specification {

    @Inject
    @Client("/")
    HttpClient client

    String accessToken

    def setup() {
        login()
    }

    private void login() {
        final credentials = new UsernamePasswordCredentials("integ-user", "test-pw")
        final request = HttpRequest.POST('/login', credentials)
        final rsp = client.toBlocking().exchange(request, BearerAccessRefreshToken)
        assert rsp.status == OK
        accessToken = rsp.body().getAccessToken()
    }
}

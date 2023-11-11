package users

import com.sgg.users.UserRegistrationRequest
import com.sgg.users.UserService
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.hateoas.GenericResource
import io.micronaut.http.hateoas.Resource
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Shared
import spock.lang.Specification

@MicronautTest
class UserRegistrationSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient client

    @Shared
    @Inject
    UserService userService

    @Shared
    @Value('${apiVersion}')
    String apiVersion

    def 'should register user'() {
        given: 'a valid registration request'
        final req = new UserRegistrationRequest("sgg-user", "test123", "test123")

        when:
        client.toBlocking().exchange(HttpRequest.POST("/" + apiVersion + "/users/register", req))

        then:
        final user = userService.getUserByUsername(req.getUsername())
        assert user != null
        assert user.getUsername() == req.getUsername()
    }

    def 'ensure validation occurs for user registration request'() {
        given:
        final req = new UserRegistrationRequest("", "test123", "test123")

        when:
        client.toBlocking()
                .exchange(HttpRequest.POST("/" + apiVersion + "/users/register", req))

        then:
        final e = thrown(HttpClientResponseException)
        assert e.getStatus() == HttpStatus.BAD_REQUEST
        final errMessage = e.getResponse().getBody(Resource).get().getEmbedded().get("errors")
                .get().get(0) as GenericResource
        assert errMessage.getAdditionalProperties().get("message") == "The provided username was blank."
    }

    def 'should throw constraint violation if username already exists'() {
        given: 'a valid registration request'
        final req = new UserRegistrationRequest("sgg-user", "test123", "test123")

        when:
        client.toBlocking()
                .exchange(HttpRequest.POST("/" + apiVersion + "/users/register", req))

        then:
        final e = thrown(HttpClientResponseException)
        assert e.getStatus() == HttpStatus.BAD_REQUEST
        final errMessage = e.getResponse().getBody(Resource).get().getEmbedded().get("errors")
            .get().get(0) as GenericResource
        assert errMessage.getAdditionalProperties().get("message") == "The username provided is already in use."

        cleanup:
        userService.deleteUser("sgg-user")
    }

}
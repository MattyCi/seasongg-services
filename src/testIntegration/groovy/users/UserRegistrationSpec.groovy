package users

import com.sgg.common.exception.SggError
import com.sgg.users.UserRegistrationRequest
import com.sgg.users.UserService
import com.sgg.users.model.UserDto
import io.micronaut.context.annotation.Value
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Get
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
    @Client(value = "/")
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

        cleanup:
        userService.deleteUser("sgg-user")
    }

    def 'ensure validation occurs for user registration request'() {
        given:
        final req = new UserRegistrationRequest("", "test123", "test123")

        when:
        client.toBlocking()
                .exchange(HttpRequest.POST("/$apiVersion/users/register", req))

        then:
        final e = thrown(HttpClientResponseException)
        assert e.getStatus() == HttpStatus.BAD_REQUEST
        final errMessage = e.getResponse().getBody(Resource).get().getEmbedded().get("errors")
                .get().get(0) as GenericResource
        assert errMessage.getAdditionalProperties().get("message") == "The provided username was blank."
    }

    def 'should throw constraint violation if username already exists'() {
        given: 'a valid registration request'
        final req = new UserRegistrationRequest("existing-user", "test123", "test123")

        when: 'a valid registration occurs'
        final validResponse = client.toBlocking()
                .exchange(HttpRequest.POST("/$apiVersion/users/register", req), UserDto)

        then: 'no errors occur'
        validResponse.getStatus() == HttpStatus.OK

        when: 'request sent with existing username'
        client.toBlocking().exchange(
                HttpRequest.POST("/$apiVersion/users/register", req),
                Argument.of(UserDto) as Argument<Object>,
                Argument.of(SggError)
        )

        then: 'validation errors are returned'
        final e = thrown(HttpClientResponseException)
        assert e.getStatus() == HttpStatus.BAD_REQUEST
        final sggError = e.getResponse().getBody(SggError.class).get()
        assert sggError.errorMessage == "The username provided is already in use."

        cleanup:
        userService.deleteUser("existing-user")
    }
}
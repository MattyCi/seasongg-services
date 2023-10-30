package users

import com.sgg.users.UserRegistrationRequest
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class DefaultUserServiceSpec extends Specification {

    @Inject
    Validator validator

    def 'should contain no validation errors for valid usernames'(String username) {
        given:
        def userRegistrationRequest = new UserRegistrationRequest(
                username, "test123", "test123"
        )

        when:
        final violations = validator.validate(userRegistrationRequest)

        then:
        assert violations.isEmpty()

        where:
        username << ["validuser", "test-user", "lionsfan123", "wingspanguy"]
    }

    def 'should return constraint violations for blank usernames'(String username) {
        given:
        def userRegistrationRequest = new UserRegistrationRequest(
                username, "test123", "test123"
        )

        when:
        final violations = validator.validate(userRegistrationRequest)

        then:
        assert !violations.isEmpty()
        violations.message.contains("The provided username was blank.")

        where:
        username << [null, "", "   ", "\t", "   "]
    }

    def 'should return constraint violations for non alphanumeric usernames'(String username) {
        given:
        def userRegistrationRequest = new UserRegistrationRequest(
                username, "test123", "test123"
        )

        when:
        final violations = validator.validate(userRegistrationRequest)

        then:
        assert !violations.isEmpty()
        violations.first().getMessage() == "Your username may only contain alphanumeric characters or hyphens."

        where:
        username << ["username123!", "inval|d", "😊user123", "user_test_123", "user\$name", "not valid"]
    }

}

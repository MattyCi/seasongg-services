package users

import com.sgg.users.UserRegistrationRequest
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class DefaultUserServiceSpec extends Specification {

    @Inject
    Validator validator;

    def 'should throw constraint violation for blank usernames'(String username) {
        given:
        def userRegistrationRequest = new UserRegistrationRequest(
                username, "test123", "test123"
        )

        when:
        final violations = validator.validate(userRegistrationRequest)

        then:
        assert !violations.isEmpty()
        violations.first().getMessage() == "The provided username was blank."

        where:
        username << [null, "", "   "]
    }

}

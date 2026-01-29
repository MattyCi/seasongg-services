package com.sgg.users

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class UserRegistrationRequestSpec extends Specification {

    @Inject
    Validator validator

    def "should throw for invalid username #username"() {
        given:
        def userRegistration = new UserRegistrationRequest(username, "password", "password")

        when:
        def errs = validator.validate(userRegistration)

        then:
        errs.size() > 0
        errs.collect { it.message }.contains(msg)

        where:
        username | msg
        "ab"     | "Your username must be between 3 and 32 characters long."
        "a" * 33 | "Your username must be between 3 and 32 characters long."
    }

    def "should throw for invalid password #password"() {
        given:
        def userRegistration = new UserRegistrationRequest("username", password, password)

        when:
        def errs = validator.validate(userRegistration)

        then:
        errs.size() > 1
        errs.collect { it.message }.contains(msg)

        where:
        password | msg
        null     | "Password must be provided."
        "  "     | "Password must be provided."
        "a" * 7  | "Password must be between 8 and 32 characters."
        "a" * 33 | "Password must be between 8 and 32 characters."
        "abc"    | "Passwords must contain at least one uppercase letter, one lowercase letter, and one number."
        "ABC"    | "Passwords must contain at least one uppercase letter, one lowercase letter, and one number."
        "123"    | "Passwords must contain at least one uppercase letter, one lowercase letter, and one number."
    }

    def "should validate if username and password valid"() {
        given:
        def userRegistration = new UserRegistrationRequest(
                "username",
                "Password123",
                "Password123"
        )

        when:
        def errs = validator.validate(userRegistration)

        then:
        errs.size() == 0
    }
}

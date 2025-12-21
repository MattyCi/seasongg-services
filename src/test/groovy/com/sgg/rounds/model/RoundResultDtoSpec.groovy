package com.sgg.rounds.model

import com.sgg.rounds.RoundDto
import com.sgg.rounds.RoundResultDto
import com.sgg.users.model.UserDto
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import jakarta.inject.Inject
import spock.lang.Specification
import spock.lang.Unroll

@MicronautTest(startApplication = false)
class RoundResultDtoSpec extends Specification {

    @Inject
    Validator validator

    def "should not have violations for a valid RoundResultDto"() {
        given:
        def roundResult = RoundResultDto.builder()
                .place(1L)
                .points(10.0)
                .round(new RoundDto())
                .build()

        when:
        def violations = validator.validate(roundResult)

        then:
        violations.isEmpty()
    }

    @Unroll
    def "validation should fail for #desc"() {
        when:
        def violations = validator.validate(roundResult)

        then:
        violations.size() == 1
        violations.message.first() == msg

        where:
        desc          | roundResult                        | msg
        "null place"  | validResult().place(null).build()  | "The place must be provided for all round results."
        "zero place"  | validResult().place(0L).build()    | "Place cannot be lower than 1."
        "21 place"    | validResult().place(21L).build()   | "Maximum place is 20."
        "null points" | validResult().points(null).build() | "Points must be provided for the round result."
        "null round"  | validResult().round(null).build()  | "The round result must be associated with a round."
        "null user"   | validResult().user(null).build()   | "The round result must be associated with a user."
    }

    def validResult() {
        return RoundResultDto.builder()
                .place(1L)
                .points(10.0)
                .user(new UserDto())
                .round(new RoundDto())
    }
}

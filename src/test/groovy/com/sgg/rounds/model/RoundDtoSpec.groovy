package com.sgg.rounds.model

import com.sgg.seasons.model.SeasonDto
import com.sgg.users.model.UserDto
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import jakarta.inject.Inject
import spock.lang.Specification
import spock.lang.Unroll

import java.time.OffsetDateTime

@MicronautTest(startApplication = false)
class RoundDtoSpec extends Specification {

    @Inject
    Validator validator

    def "should not have violations for #desc"() {
        when:
        def violations = validator.validate(round)

        then:
        violations.isEmpty()

        where:
        desc                    | round
        "valid round"           | validResult().build()
        "valid round with ties" | validResultWithTies().build()
    }

    @Unroll
    def "validation should fail for #desc"() {
        when:
        def violations = validator.validate(round)

        then:
        violations.size() == 1
        violations.message.first() == msg

        where:
        desc                    | round                                     | msg
        "null roundDate"        | validResult().roundDate(null).build()     | "The round must have a date associated with it."
        "null roundResults"     | validResult().roundResults(null).build()  | "The round results must be provided."
        "too few roundResults"  | tooFewRoundResults()                      | "Round results must be between 2 and 31."
        "too many roundResults" | tooManyRoundResult()                      | "Round results must be between 2 and 31."
        "null creator"          | validResult().creator(null).build()       | "The round must be associated with a user."
        "missing place"         | roundResultsWithMissingPlace()            | "There are missing places in the round results. Double-check the order of the places and try again."
        "duplicate players"     | roundResultsWithDuplicatePlayers()        | "A user cannot have multiple results in the same round."
    }

    def static validResult() {
        return RoundDto.builder()
                .roundDate(OffsetDateTime.parse("3000-04-17T22:00:00-05:00"))
                .roundResults([
                        RoundResultDto.builder()
                        .user(new UserDto(userId: 1, username: "player1"))
                        .place(1).build(),
                        RoundResultDto.builder()
                                .user(new UserDto(userId: 2, username: "player2"))
                                .place(2).build(),
                        RoundResultDto.builder()
                                .user(new UserDto(userId: 3, username: "player3"))
                                .place(3).build(),
                ])
                .season(new SeasonDto())
                .creator(new UserDto())
    }

    def validResultWithTies() {
        return RoundDto.builder()
                .roundDate(OffsetDateTime.parse("3000-04-17T22:00:00-05:00"))
                .roundResults([
                        RoundResultDto.builder()
                                .user(new UserDto(userId: 1, username: "player1"))
                                .place(1).build(),
                        RoundResultDto.builder()
                                .user(new UserDto(userId: 2, username: "player2"))
                                .place(2).build(),
                        RoundResultDto.builder()
                                .user(new UserDto(userId: 3, username: "player3"))
                                .place(2).build(),
                        RoundResultDto.builder()
                                .user(new UserDto(userId: 4, username: "player4"))
                                .place(3).build(),
                ])
                .season(new SeasonDto())
                .creator(new UserDto())
    }

    def tooFewRoundResults() {
        def builder = validResult()
        builder.roundResults([
            RoundResultDtoSpec.validResult().build()
        ])
        builder.build()
    }

    def static tooManyRoundResult() {
        def builder = validResult()
        def roundResults = []
        for (i in 0..< 31) {
            roundResults.add(RoundResultDtoSpec
                    .validResult()
                    .user(new UserDto(userId: i, username: "player${i}"))
                    .build())
        }
        builder.roundResults(roundResults).build()
    }

    def roundResultsWithMissingPlace() {
        def builder = validResult()
        builder.roundResults([
                RoundResultDtoSpec.validResult().user(new UserDto(userId: 1)).place(1).build(),
                RoundResultDtoSpec.validResult().user(new UserDto(userId: 2)).place(3).build()
        ])
        builder.build()
    }

    def roundResultsWithDuplicatePlayers() {
        def builder = validResult()
        builder.roundResults([
                RoundResultDtoSpec.validResult().user(new UserDto(userId: 1)).build(),
                RoundResultDtoSpec.validResult().user(new UserDto(userId: 1)).build(),
                RoundResultDtoSpec.validResult().user(new UserDto(userId: 2)).build()
        ])
        builder.build()
    }
}

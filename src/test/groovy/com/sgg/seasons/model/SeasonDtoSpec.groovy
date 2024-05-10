package com.sgg.seasons.model

import com.sgg.games.model.GameDto
import com.sgg.users.model.UserDto
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import jakarta.inject.Inject
import spock.lang.Shared
import spock.lang.Specification

import java.time.OffsetDateTime

@MicronautTest(startApplication = false)
class SeasonDtoSpec extends Specification {

    @Inject
    Validator validator

    def "should not throw for valid season"() {
        given:
        def season = SeasonDto.builder()
            .name(name)
            .startDate(OffsetDateTime.now())
            .endDate(OffsetDateTime.now())
            .creator(new UserDto(1, "some-user", OffsetDateTime.now()))
            .status(SeasonStatus.ACTIVE)
            .game(new GameDto(1, "some-game", []))
            .build()

        when:
        def errs = validator.validate(season)

        then:
        errs.size() == 0

        where:
        name << [
                "valid",
                "season-test-name",
                "season_test_name",
                "season?",
                "season0123456789",
                "season@!&\$"
        ]
    }

    def "should throw validation error for invalid seasons"() {
        when:
        def errs = validator.validate(season)

        then:
        errs.size() > 0
        errs.message.contains(msg)

        where:
        season | msg
        new SeasonDto(name: "")          | "The season name must not be blank."
        new SeasonDto(name: "  ")        | "The season name must not be blank."
        new SeasonDto(name: null)        | "The season name must not be blank."
        new SeasonDto(name: "aa")        | "The season name must be between 3 and 56 characters."
        new SeasonDto(name: "a" * 57)    | "The season name must be between 3 and 56 characters."
        new SeasonDto(name: "abcdefg_%") | "Invalid characters detected in the season name."
        new SeasonDto(name: "abcdefg_<") | "Invalid characters detected in the season name."
        new SeasonDto(name: "abcdefg_*") | "Invalid characters detected in the season name."
        new SeasonDto(name: "abcdefg\n") | "Invalid characters detected in the season name."
        noStartDateSeason                | "The season must have a start date."
        noEndDateSeason                  | "The season must have an end date."
        new SeasonDto(creator: null)     | "The season must have a creator associated with it."
        new SeasonDto(status: null)      | "The season status was not provided."
        new SeasonDto(game: null)        | "The season must have a game associated with it."
        endDateBeforeStartSeason         | "Please choose a date in the future for your season end date."
    }

    @Shared
    SeasonDto endDateBeforeStartSeason = new SeasonDto(
            startDate: OffsetDateTime.parse("2010-12-25T00:00:00Z"),
            endDate: OffsetDateTime.parse("2010-12-01T00:00:00Z")
    )

    @Shared
    SeasonDto noStartDateSeason = new SeasonDto(
            startDate: null,
            endDate: OffsetDateTime.parse("2010-12-01T00:00:00Z")
    )

    @Shared
    SeasonDto noEndDateSeason = new SeasonDto(
            startDate: OffsetDateTime.parse("2010-12-01T00:00:00Z"),
            endDate: null
    )
}

package com.sgg.games.model

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class GameDtoSpec extends Specification {

    @Inject
    Validator validator

    def "should not throw for valid games"() {
        given:
        def game = GameDto.builder()
                .name(name)
                .gameId(gameId)
                .build()

        when:
        def errs = validator.validate(game)

        then:
        errs.size() == 0

        where:
        gameId | name
        1      | "Catan"
        2      | "Ticket to Ride"
        3      | "Carcassonne"
        4      | "Risk"
        5      | "Chess"
        6      | "Monopoly"
        7      | "Pandemic"
        8      | "Clue"
        9      | "Dominion"
        10     | "Azul"
        11     | "Codenames: Duet"
        12     | "7 Wonders"
        13     | "Dixit: Odyssey"
        14     | "Tsuro: The Game of the Path"
        15     | "Kingdomino"
        16     | "T.I.M.E Stories"
        17     | "Love Letter: Premium Edition"
        18     | "Splendor: Marvel"
        19     | "Catan: Seafarers"
        20     | "Santorini: New York"
        21     | "Tokaido"
        22     | "Kanban EV"
        23     | "Ra"
        24     | "Patchwork Doodle"
        25     | "Kemet"
        26     | "Santorini"
    }

    def "should throw validation error for null game IDs"() {
        when:
        def errs = validator.validate(new GameDto(null, "name", null))

        then:
        errs.first().message == "The game ID is required."
    }

    def "should throw validation error for negative game IDs"() {
        when:
        def errs = validator.validate(new GameDto(-1, "name", null))

        then:
        errs.first().message == "The game ID must be positive."
    }

    def "should throw validation error for null game name"() {
        when:
        def errs = validator.validate(new GameDto(1, null, null))

        then:
        errs.first().message == "The game must have a name associated with it."
    }

    def "should throw validation error for invalid game names"() {
        when:
        def errs = validator.validate(new GameDto(1, name, null))

        then:
        errs.size() == 1

        and:
        errs.first().message == "Invalid game name provided."

        where:
        name << [
                "",
                "@mazing Game",
                "Game#1",
                "%Complete",
                "Fun^Times",
                "\$Dollar Deals",
                "Chess_Advanced",
                "Hello<World>",
                "[Escape|Room]",
                "Good~Game",
                "CheckItOut=`True`",
                "What’sUp?",
                "Win->Lose",
                "{Mystery}{Box}",
                "Big\$\$\$Wins",
                "Party~Time Deluxe",
                "|Pipes|Everywhere|",
                "Victory<Skill>",
                "Game#Over#Again",
                "`QuotedGame`",
                "Code!Rush@Night",
                "Slash/Backslash\\Combo",
                "Half~Life_Game"
        ]
    }
}

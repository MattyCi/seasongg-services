package com.sgg.seasons

import com.sgg.rounds.model.RoundResultDao
import spock.lang.Specification

import static com.sgg.DaoFixtures.*

class ScoringServiceSpec extends Specification {

    ScoringService scoringService = new ScoringService()

    def "should calculate points for place #place"() {
        given:
        def result = new RoundResultDao(place: place)

        when:
        scoringService.calculatePoints(result)

        then:
        result.points == expectedPoints

        where:
        place | expectedPoints
        1     | 10
        2     | 9
        3     | 8
        4     | 7
        5     | 6
        6     | 5
        7     | 4
        8     | 3
        9     | 2
        10    | 1
        11    | 1
    }

    def "should return ordered scores and places for season standings"() {
        given:
        def season = validSeason()
        season.rounds = [
            roundWith([
                    roundResult(10, matty()),
                    roundResult(9, aaron()),
                    roundResult(8, mistalegit())
            ]),
            roundWith([
                    roundResult(10, aaron()),
                    roundResult(9, mistalegit()),
                    roundResult(8, matty())
            ]),
            roundWith([
                    roundResult(10, mistalegit()),
                    roundResult(9, aaron()),
                    roundResult(8, matty())
            ])
        ]

        when:
        def standings = scoringService.calculateStandings(season)

        then:
        standings.size() == 3
        standings[0].user.username == "aaron"
        standings[0].place == 1
        standings[0].points == 9.33

        and:
        standings[1].user.username == "mistalegit"
        standings[1].place == 2
        standings[1].points == 9

        and:
        standings[2].user.username == "matty"
        standings[2].place == 3
        standings[2].points == 8.66
    }

    def "should account for ties in season standings"() {
        given:
        def season = validSeason()
        season.rounds = [
                roundWith([
                        roundResult(10, matty()),
                        roundResult(9, aaron()),
                        roundResult(8, mistalegit())
                ]),
                roundWith([
                        roundResult(10, aaron()),
                        roundResult(9, matty()),
                        roundResult(8, mistalegit())
                ]),
                roundWith([
                        roundResult(10, mistalegit()),
                        roundResult(9, aaron()),
                        roundResult(9, matty())
                ])
        ]

        when:
        def standings = scoringService.calculateStandings(season)

        then:
        standings.size() == 3

        and: "tie for first place"
        standings.subList(0, 2).collect { it.user.username }.containsAll("matty", "aaron")
        standings[0].place == 1
        standings[1].place == 1
        standings[0].points == 9.33
        standings[1].points == 9.33

        and:
        standings[2].user.username == "mistalegit"
        standings[2].place == 3
        standings[2].points == 8.66
    }

    def "should set ineligible players if they haven't played enough rounds"() {
        given:
        def season = validSeason()
        season.rounds = [
                roundWith([
                        roundResult(10, matty()),
                        roundResult(9, aaron()),
                ]),
                roundWith([
                        roundResult(10, aaron()),
                        roundResult(9, matty()),
                ]),
                roundWith([
                        roundResult(10, aaron()),
                        roundResult(9, matty()),
                ]),
                roundWith([
                        roundResult(10, aaron()),
                        roundResult(9, matty()),
                        roundResult(8, dan())
                ])
        ]

        when:
        def standings = scoringService.calculateStandings(season)

        then:
        standings.size() == 3

        and: "player who only played one round is ineligible"
        standings[2].place == 999
    }
}

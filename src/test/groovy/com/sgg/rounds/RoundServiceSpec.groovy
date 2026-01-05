package com.sgg.rounds

import com.sgg.common.exception.ClientException
import com.sgg.common.exception.NotFoundException
import com.sgg.games.ExternalGameClient
import com.sgg.games.GameRepository
import com.sgg.games.GameService
import com.sgg.games.model.GameMapper
import com.sgg.rounds.model.RoundDao
import com.sgg.rounds.model.RoundDto
import com.sgg.rounds.model.RoundDtoSpec
import com.sgg.rounds.model.RoundResultDto
import com.sgg.seasons.SeasonRepository
import com.sgg.seasons.SeasonService
import com.sgg.seasons.model.SeasonDto
import com.sgg.seasons.model.SeasonMapper
import com.sgg.seasons.model.SeasonStatus
import com.sgg.users.UserMapper
import com.sgg.users.UserService
import com.sgg.users.authz.PermissionService
import com.sgg.users.model.UserDto
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class RoundServiceSpec extends Specification {
    @Inject
    Validator validator
    RoundRepository roundRepository = Mock()
    UserService userService = Mock()
    RoundMapper roundMapper = new RoundMapperImpl()
    GameService gameService = Mock(constructorArgs: [Mock(GameRepository), Mock(GameMapper), Mock(ExternalGameClient)])
    SeasonService seasonService = Mock(constructorArgs: [
            Mock(SeasonRepository),
            gameService,
            Mock(UserService),
            Mock(SeasonMapper),
            validator,
            Mock(UserMapper),
            Mock(PermissionService)
    ])

    RoundService roundService

    def setup() {
        roundService = new RoundService(
                roundRepository,
                userService,
                roundMapper,
                validator,
                seasonService
        )
    }

    def "should get round"() {
        given:
        def dao = RoundDao.builder().roundId(123L).build()

        when:
        def result = roundService.getRound(123L)

        then:
        1 * roundRepository.findById(123L) >> Optional.of(dao)
        0 * _
        result.roundId == 123L
    }

    def "should throw not found for round that doesn't exist"() {
        when:
        roundService.getRound(123L)

        then:
        1 * roundRepository.findById(123L) >> Optional.empty()
        0 * _
        def e = thrown(NotFoundException)
        e.message == "No round was found for the given ID."
    }

    def "should create round"() {
        given:
        def season = SeasonDto.builder().seasonId(1L).name("season1").status(SeasonStatus.ACTIVE).build()
        def results = (1..11).collect { i ->
            RoundResultDto.builder().place(i).user(UserDto.builder().userId(i).build()).build()
        }
        def roundDto = RoundDto.builder()
                .roundResults(results)
                .build()

        when:
        def result = roundService.createRound("1", roundDto)

        then:
        1 * seasonService.getSeason("1") >> season
        1 * userService.getCurrentUser() >> UserDto.builder().userId(99).username("creator").build()
        1 * roundRepository.save({ RoundDao r ->
            assert r.getRoundResults().size() == 11
        }) >> { args -> args[0] }
        11 * userService.getUserById(_) >> { Long id -> UserDto.builder().userId(id).build() }
        0 * _

        and:
        result.roundResults.size() == 11
        result.creator.userId == 99

        and: "results have points calculated"
        def sorted = result.getRoundResults().sort { it.place }
        sorted[0].points == 10
        sorted[1].points == 9
        sorted[2].points == 8
        sorted[3].points == 7
        sorted[4].points == 6
        sorted[5].points == 5
        sorted[6].points == 4
        sorted[7].points == 3
        sorted[8].points == 2
        sorted[9].points == 1
        sorted[10].points == 1
    }

    def "should not create round when season is inactive"() {
        given:
        def season = SeasonDto.builder().seasonId(1L).name("inactive season").status(SeasonStatus.INACTIVE).build()
        def result1 = RoundResultDto.builder().place(1).user(UserDto.builder().userId(1L).build()).build()
        def result2 = RoundResultDto.builder().place(2).user(UserDto.builder().userId(2L).build()).build()
        def roundDto = RoundDto.builder()
                .roundResults([result1, result2])
                .build()

        when:
        roundService.createRound("1", roundDto)

        then:
        1 * seasonService.getSeason("1") >> season
        0 * _
        def e = thrown(ClientException)
        e.message == "Rounds cannot be created because the season has ended."
    }

    def "should throw when round validation fails"() {
        given:
        def invalidRound = new RoundDto(
                roundResults: []
        )

        when:
        roundService.createRound("1", invalidRound)

        then:
        1 * seasonService.getSeason("1") >> new SeasonDto(seasonId: 1, name: "season", status: SeasonStatus.ACTIVE)
        1 * userService.getCurrentUser() >> UserDto.builder().userId(99L).username("creator").build()
        0 * _
        def e = thrown(ClientException)
        e.message == "Round results must be between 2 and 31."
    }

    def "should throw for too many round results"() {
        given:
        def invalidRound = RoundDtoSpec.tooManyRoundResult()

        when:
        roundService.createRound("1", invalidRound)

        then:
        1 * seasonService.getSeason("1") >> new SeasonDto(seasonId: 1, name: "season", status: SeasonStatus.ACTIVE)
        1 * userService.getCurrentUser() >> UserDto.builder().userId(99L).username("creator").build()
        0 * _
        def e = thrown(ClientException)
        e.message == "Round results must be between 2 and 31."
    }

    def "should throw client exception when a player does not exist"() {
        given:
        def result1 = RoundResultDto.builder().place(1)
                .user(UserDto.builder().userId(404).username("non-existent").build())
                .build()
        def result2 = RoundResultDto.builder().place(2)
                .user(UserDto.builder().userId(123).username("some-user").build())
                .build()
        def roundDto = RoundDto.builder()
                .roundResults([result1, result2])
                .build()

        when:
        roundService.createRound("1", roundDto)

        then:
        1 * seasonService.getSeason("1") >> SeasonDto.builder().seasonId(1L).name("season").status(SeasonStatus.ACTIVE).build()
        1 * userService.getCurrentUser() >> UserDto.builder().userId(99L).username("creator").build()
        1 * userService.getUserById(404L) >> { throw new NotFoundException("User not found!") }
        0 * _
        def e = thrown(ClientException)
        e.message == "Failed to create round because the player non-existent in place 1 does not exist."
    }

    def "should use season service to remove round"() {
        when:
        roundService.deleteRound("123", "456")

        then:
        1 * seasonService.removeRound("123", "456")
        1 * userService.getCurrentUser() >> new UserDto(username: "test")
        0 * _
    }
}

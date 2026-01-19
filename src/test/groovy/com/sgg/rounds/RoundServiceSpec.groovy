package com.sgg.rounds


import com.sgg.common.exception.NotFoundException
import com.sgg.games.ExternalGameClient
import com.sgg.games.GameRepository
import com.sgg.games.GameService
import com.sgg.games.model.GameMapper
import com.sgg.rounds.model.RoundDao
import com.sgg.rounds.model.RoundDtoSpec
import com.sgg.seasons.ScoringService
import com.sgg.seasons.SeasonRepository
import com.sgg.seasons.SeasonService
import com.sgg.seasons.model.SeasonDto
import com.sgg.seasons.model.SeasonMapper
import com.sgg.users.UserMapper
import com.sgg.users.UserService
import com.sgg.users.authz.PermissionService
import com.sgg.users.model.UserDto
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import spock.lang.Specification

@MicronautTest(startApplication = false)
class RoundServiceSpec extends Specification {
    RoundRepository roundRepository = Mock()
    UserService userService = Mock()
    RoundMapper roundMapper = new RoundMapperImpl()
    GameService gameService = Mock(constructorArgs: [Mock(GameRepository), Mock(GameMapper), Mock(ExternalGameClient)])
    SeasonService seasonService = Mock(constructorArgs: [
            Mock(SeasonRepository),
            gameService,
            Mock(UserService),
            Mock(SeasonMapper),
            Mock(Validator),
            Mock(UserMapper),
            Mock(PermissionService),
            Mock(RoundMapper),
            Mock(ScoringService)
    ])

    RoundService roundService

    def setup() {
        roundService = new RoundService(
                roundRepository,
                userService,
                roundMapper,
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

    def "should use season service to add round"() {
        given:
        def roundDto = RoundDtoSpec.validResult().build()

        when:
        roundService.addRound("123", roundDto)

        then:
        1 * seasonService.addRound("123", roundDto) >> new SeasonDto()
        0 * _
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

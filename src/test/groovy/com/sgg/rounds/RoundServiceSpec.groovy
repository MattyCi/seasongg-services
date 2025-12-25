package com.sgg.rounds

import com.sgg.common.exception.ClientException
import com.sgg.common.exception.NotFoundException
import com.sgg.rounds.model.RoundDao
import com.sgg.rounds.model.RoundDto
import com.sgg.rounds.model.RoundResultDto
import com.sgg.seasons.SeasonService
import com.sgg.seasons.model.SeasonDto
import com.sgg.seasons.model.SeasonStatus
import com.sgg.users.UserService
import com.sgg.users.model.UserDto
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import spock.lang.Specification

@MicronautTest(startApplication = false)
class RoundServiceSpec extends Specification {
    RoundRepository roundRepository = Mock()
    UserService userService = Mock()
    RoundMapper roundMapper = new RoundMapperImpl()
    Validator validator = Mock()
    SeasonService seasonService = Mock()

    RoundService roundService = new RoundService(
            roundRepository,
            userService,
            roundMapper,
            validator,
            seasonService
    )

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

    def "should create round when season is active and players exist"() {
        given:
        def r1 = RoundResultDto.builder().place(1L).points(10.0).user(UserDto.builder().userId(1L).username("p1").build()).build()
        def r2 = RoundResultDto.builder().place(2L).points(5.0).user(UserDto.builder().userId(2L).username("p2").build()).build()
        def roundDto = RoundDto.builder()
                .roundResults([r1, r2])
                .build()

        when:
        def result = roundService.createRound("1", roundDto)

        then: "season and creator are retrieved, validation passes, players resolved and DAO saved"
        1 * seasonService.getSeason("1") >> SeasonDto.builder().seasonId(1L).name("S").status(SeasonStatus.ACTIVE).build()
        1 * userService.getCurrentUser() >> UserDto.builder().userId(99L).username("creator").build()
        1 * validator.validate(roundDto) >> []
        1 * roundRepository.save({ RoundDao s ->
            assert s.getRoundResults().size() == 2
            assert s.getRoundResults().every { rr -> rr.getRound() == s }
        }) >> { args -> args[0] }
        2 * userService.getUserById(_) >> { Long id -> UserDto.builder().userId(id).username("user${id}").build() }
        0 * _

        and:
        result.roundResults.size() == 2
        result.creator.userId == 99L
    }

    def "should not create round when season is inactive"() {
        given:
        def r = RoundResultDto.builder().place(1L).points(10.0).user(UserDto.builder().userId(1L).username("p").build()).build()
        def roundDto = RoundDto.builder().roundResults([r]).build()

        when:
        roundService.createRound("1", roundDto)

        then:
        1 * seasonService.getSeason("1") >> SeasonDto.builder().seasonId(1L).name("S").status(SeasonStatus.INACTIVE).build()
        0 * _
        def e = thrown(ClientException)
        e.message == "Rounds cannot be created because the season has ended."
    }

    def "should throw client exception when round validation fails"() {
        given:
        def r = RoundResultDto.builder().place(1L).points(10.0).user(UserDto.builder().userId(1L).username("p").build()).build()
        def roundDto = RoundDto.builder().roundResults([r]).build()
        def violation = Mock(javax.validation.ConstraintViolation) { getMessage() >> "bad" }

        when:
        roundService.createRound("1", roundDto)

        then:
        1 * seasonService.getSeason("1") >> SeasonDto.builder().seasonId(1L).name("S").status(SeasonStatus.ACTIVE).build()
        1 * userService.getCurrentUser() >> UserDto.builder().userId(99L).username("creator").build()
        1 * validator.validate(roundDto) >> [violation]
        0 * _
        def e = thrown(ClientException)
        e.message == "bad"
    }

    def "should throw client exception when a player does not exist"() {
        given:
        def r = RoundResultDto.builder().place(3L).points(1.0).user(UserDto.builder().userId(404L).username("ghost").build()).build()
        def roundDto = RoundDto.builder().roundResults([r]).build()

        when:
        roundService.createRound("1", roundDto)

        then:
        1 * seasonService.getSeason("1") >> SeasonDto.builder().seasonId(1L).name("S").status(SeasonStatus.ACTIVE).build()
        1 * userService.getCurrentUser() >> UserDto.builder().userId(99L).username("creator").build()
        1 * validator.validate(roundDto) >> []
        1 * userService.getUserById(404L) >> { throw new NotFoundException("User not found!") }
        0 * _
        def e = thrown(ClientException)
        e.message == "Failed to create round because the player ghost in place 3 does not exist."
    }
}

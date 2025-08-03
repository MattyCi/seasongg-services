package com.sgg.seasons.model

import com.sgg.common.exception.ClientException
import com.sgg.common.exception.NotFoundException
import com.sgg.games.GameRepository
import com.sgg.games.model.GameMapper
import com.sgg.seasons.SeasonRepository
import com.sgg.seasons.SeasonService
import com.sgg.users.UserMapper
import com.sgg.users.UserService
import com.sgg.users.authz.PermissionRepository
import com.sgg.users.authz.UserPermissionRepository
import io.micronaut.security.utils.SecurityService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class SeasonServiceSpec extends Specification {

    SeasonRepository seasonRepository = Mock()
    SeasonMapper seasonMapper = Mock()

    @Inject
    Validator validator

    SeasonService seasonService = new SeasonService(
            seasonRepository,
            Mock(GameRepository),
            Mock(SecurityService),
            Mock(UserService),
            seasonMapper,
            Mock(GameMapper),
            validator,
            Mock(UserMapper),
            Mock(PermissionRepository),
            Mock(UserPermissionRepository)
    )

    def "should get season"() {
        given:
        def dao = SeasonDao.builder().seasonId(123).build()

        when:
        def result = seasonService.getSeason("123")

        then:
        1 * seasonRepository.findById(123) >> Optional.of(dao)
        1 * seasonMapper.toSeasonDto(dao) >> SeasonDto.builder().seasonId(123).build()
        0 * _
        result.seasonId == 123
    }

    def "should throw not found for season that doesn't exist"() {
        when:
        seasonService.getSeason("123")

        then:
        1 * seasonRepository.findById(123) >> Optional.empty()
        0 * _
        def e = thrown(NotFoundException)
        e.message == "No season was found for the given ID."
    }

    def "should throw error for invalid input for season ID"() {
        when:
        seasonService.getSeason("123invalid")

        then:
        0 * _
        def e = thrown(ClientException)
        e.message == "Invalid seasonId provided."
    }
}

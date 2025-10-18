package com.sgg.seasons.model

import com.sgg.common.exception.ClientException
import com.sgg.common.exception.NotFoundException
import com.sgg.games.GameRepository
import com.sgg.games.model.GameMapper
import com.sgg.seasons.SeasonRepository
import com.sgg.seasons.SeasonService
import com.sgg.users.UserDao
import com.sgg.users.UserMapper
import com.sgg.users.UserMapperImpl
import com.sgg.users.UserService
import com.sgg.users.authz.*
import io.micronaut.security.utils.SecurityService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import spock.lang.Specification

import java.time.OffsetDateTime

@MicronautTest(startApplication = false)
class SeasonServiceSpec extends Specification {

    SeasonRepository seasonRepository = Mock()
    UserService userService = Mock()
    SeasonMapper seasonMapper = new SeasonMapperImpl()
    Validator validator = Mock()
    UserMapper userMapper = new UserMapperImpl()
    PermissionService permissionService = Mock()

    SeasonService seasonService = new SeasonService(
            seasonRepository,
            Mock(GameRepository),
            Mock(SecurityService),
            userService,
            seasonMapper,
            Mock(GameMapper),
            validator,
            userMapper,
            permissionService
    )

    def "should get season"() {
        given:
        def dao = SeasonDao.builder().seasonId(123).build()

        when:
        def result = seasonService.getSeason("123")

        then:
        1 * seasonRepository.findById(123) >> Optional.of(dao)
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

    def "should update season admin and season end date"() {
        given:
        def oldAdmin = UserDao.builder().userId(123).username("old-admin").build()
        def newAdmin = UserDao.builder().userId(456).username("new-admin").build()
        def oldSeason = SeasonDao.builder()
                .seasonId(999)
                .creator(oldAdmin)
                .startDate(OffsetDateTime.parse("2999-01-01T00:00:00-00:00"))
                .endDate(OffsetDateTime.parse("3000-01-01T00:00:00-00:00"))
                .status("ACTIVE")
                .name("Season")
                .build()
        def newSeason = SeasonDao.builder()
                .seasonId(999)
                .creator(newAdmin)
                .startDate(OffsetDateTime.parse("2999-01-01T00:00:00-00:00"))
                .endDate(OffsetDateTime.parse("2000-01-01T00:00:00-00:00"))
                .status("ACTIVE") // should change to inactive based on new end date
                .name("Season")
                .build()
        def newSeasonDto = seasonMapper.toSeasonDto(newSeason)

        when:
        seasonService.updateSeason("999", newSeasonDto)

        then:
        1 * validator.validate(newSeasonDto) >> []
        1 * seasonRepository.findById(999) >> Optional.of(oldSeason)
        1 * userService.getUserById(456) >> userMapper.userToUserDto(newAdmin)
        1 * permissionService.swapSeasonAdmins(999, { UserDao o ->
            o.userId == 123
            o.username == "old-admin"
        }, { UserDao n ->
            n.userId == 456
            n.username == "new-admin"
        })

        and: "season has been updated with new admin and inactivated due to date change"
        1 * seasonRepository.update({ SeasonDao s ->
            s.creator.userId == 456
            s.status == "INACTIVE"
        }) >> newSeason
        0 * _
    }

    def "should throw if new season admin doesn't exist"() {
        given:
        def creator = new UserDao(userId: 123, username: "matty")
        def oldSeason = SeasonDao.builder()
                .seasonId(999)
                .creator(creator)
                .build()
        def newSeason = SeasonDao.builder()
                .seasonId(999)
                .creator(new UserDao(userId: 404))
                .build()
        def newSeasonDto = seasonMapper.toSeasonDto(newSeason)

        when:
        seasonService.updateSeason("999", newSeasonDto)

        then:
        1 * validator.validate(newSeasonDto) >> []
        1 * seasonRepository.findById(999) >> Optional.of(oldSeason)
        1 * userService.getUserById(404) >> { throw new NotFoundException("User not found!") }
        0 * _
        def e = thrown(NotFoundException)
        e.message == "The given user for the season admin does not exist."
    }

    def "should re-activate season if season end date is extended past the current date"() {
        given:
        def creator = new UserDao(userId: 123, username: "matty")
        def oldSeason = SeasonDao.builder()
                .seasonId(999)
                .creator(creator)
                .startDate(OffsetDateTime.parse("2000-01-01T00:00:00-00:00"))
                .endDate(OffsetDateTime.parse("2020-01-01T00:00:00-00:00"))
                .status("INACTIVE")
                .name("Catan Tournament")
                .build()
        def newSeason = SeasonDao.builder()
                .seasonId(999)
                .creator(creator)
                .startDate(OffsetDateTime.parse("2000-01-01T00:00:00-00:00"))
                .endDate(OffsetDateTime.parse("3000-01-01T00:00:00-00:00"))
                .status("INACTIVE")
                .name("Catan Tournament")
                .build()
        def newSeasonDto = seasonMapper.toSeasonDto(newSeason)

        when:
        def updated = seasonService.updateSeason("999", newSeasonDto)

        then:
        1 * validator.validate(newSeasonDto) >> []
        1 * seasonRepository.findById(999) >> Optional.of(oldSeason)

        and: "season should be re-activated"
        1 * seasonRepository.update({ SeasonDao s ->
            s.status == "ACTIVE"
        }) >> newSeason

        and:
        0 * _
        updated.endDate == OffsetDateTime.parse("3000-01-01T00:00:00-00:00")
    }
}

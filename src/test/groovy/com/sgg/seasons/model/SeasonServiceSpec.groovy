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
import com.sgg.users.authz.PermissionDao
import com.sgg.users.authz.PermissionRepository
import com.sgg.users.authz.PermissionType
import com.sgg.users.authz.ResourceType
import com.sgg.users.authz.UserPermissionDao
import com.sgg.users.authz.UserPermissionRepository
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
    PermissionRepository permissionRepository = Mock()
    UserPermissionRepository userPermissionRepository = Mock()

    SeasonService seasonService = new SeasonService(
            seasonRepository,
            Mock(GameRepository),
            Mock(SecurityService),
            userService,
            seasonMapper,
            Mock(GameMapper),
            validator,
            userMapper,
            permissionRepository,
            userPermissionRepository
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
        def adminPermission = Mock(PermissionDao)
        def oldUserPerm = Mock(UserPermissionDao)

        when:
        def updated = seasonService.updateSeason("999", newSeasonDto)

        then:
        1 * validator.validate(newSeasonDto) >> []
        1 * seasonRepository.findById(999) >> Optional.of(oldSeason)
        1 * userService.getUserById(456) >> userMapper.userToUserDto(newAdmin)
        1 * permissionRepository.findByResourceIdAndResourceTypeAndPermissionType(
                999, ResourceType.SEASON, PermissionType.WRITE
        ) >> Optional.of(adminPermission)
        1 * userPermissionRepository.findByUserDaoAndPermissionDao({ UserDao u ->
            u.userId == 123
        }, adminPermission) >> Optional.of(oldUserPerm)
        1 * userPermissionRepository.delete(oldUserPerm)
        1 * userPermissionRepository.save({ UserPermissionDao up ->
            up.userDao.userId == 456
            up.permissionDao == adminPermission
        })

        and: "season has been updated with new admin and inactivated due to date change"
        1 * seasonRepository.update({ SeasonDao s ->
            s.creator.userId == 456
            s.status == "INACTIVE"
        }) >> newSeason
        0 * _
    }
}

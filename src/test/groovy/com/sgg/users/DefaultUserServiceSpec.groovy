package com.sgg.users

import com.sgg.DaoFixtures
import com.sgg.DtoFixtures
import com.sgg.common.exception.ClientException
import com.sgg.users.authz.*
import com.sgg.users.security.PasswordEncoder
import io.micronaut.security.utils.SecurityService
import io.micronaut.validation.validator.Validator
import spock.lang.Specification

class DefaultUserServiceSpec extends Specification {

    PermissionRepository permissionRepository = Mock()
    UserPermissionRepository userPermissionRepository = Mock()

    UserRepository userRepository = Mock()
    UserMapper userMapper = Mock()
    PasswordEncoder passwordEncoder = Mock()
    SecurityService securityService = Mock()
    Validator validator = Mock()

    DefaultPermissionService defaultPermissionService = new DefaultPermissionService(
            permissionRepository,
            userPermissionRepository
    )

    DefaultUserService userService = new DefaultUserService(userRepository, userMapper, passwordEncoder, securityService, validator)

    def "should register user"() {
        given:
        def request = new UserRegistrationRequest("matty", "password", "password")
        def userDto = DtoFixtures.matty()

        when:
        def result = userService.registerUser(request)

        then:
        1 * validator.validate(request) >> []
        1 * userRepository.findByUsernameIgnoreCase("matty") >> Optional.empty()
        1 * passwordEncoder.encode("password") >> "hashed"
        1 * userRepository.save(_)
        1 * userMapper.userToUserDto(_) >> userDto
        0 * _
        result.username == "matty"
    }

    def "should not register user if passwords do not match"() {
        given:
        def request = new UserRegistrationRequest("matty", "password", "not-matching")

        when:
        userService.registerUser(request)

        then:
        1 * validator.validate(request) >> []
        0 * _
        def e = thrown(ClientException)
        e.message == "The provided passwords do not match."
    }

    def "should not register user if username already in use"() {
        given:
        def request = new UserRegistrationRequest("matty", "password", "password")

        when:
        userService.registerUser(request)

        then:
        1 * validator.validate(request) >> []
        1 * userRepository.findByUsernameIgnoreCase("matty") >> Optional.of(DaoFixtures.matty())
        0 * _
        def e = thrown(ClientException)
        e.message == "The username provided is already in use."
    }

    def "should create season admin permissions for user"() {
        given:
        def seasonId = 999
        def admin = new UserDao(userId: 123, username: "some-admin")

        def expectedPermission = new PermissionDao(
                permId: 0,
                resourceId: 999,
                resourceType: ResourceType.SEASON,
                permissionType:  PermissionType.WRITE
        )
        def expectedUserPermission = new UserPermissionDao(userDao: admin, permissionDao: expectedPermission)

        when:
        defaultPermissionService.insertSeasonAdminPermission(seasonId, admin)

        then:
        1 * permissionRepository.save(expectedPermission)
        1 * userPermissionRepository.save(expectedUserPermission)
    }

    def "should swap season admins"() {
        given:
        def seasonId = 999
        def oldAdmin = new UserDao(userId: 123, username: "old-admin")
        def newAdmin = new UserDao(userId: 456, username: "new-admin")

        def writePermission = new PermissionDao(
                resourceType:  ResourceType.SEASON,
                permissionType:  PermissionType.WRITE,
                resourceId:  seasonId
        )
        def oldUserPerm = new UserPermissionDao(userDao: oldAdmin, permissionDao: writePermission)
        def newUserPerm = new UserPermissionDao(
                userDao: newAdmin,
                permissionDao: writePermission
        )

        when:
        defaultPermissionService.swapSeasonAdmins(seasonId, oldAdmin, newAdmin)

        then:
        1 * permissionRepository.findByResourceIdAndResourceTypeAndPermissionType(
                seasonId, ResourceType.SEASON, PermissionType.WRITE
        ) >> Optional.of(writePermission)
        1 * userPermissionRepository.findByUserDaoAndPermissionDao(oldAdmin, writePermission) >> Optional.of(oldUserPerm)
        1 * userPermissionRepository.delete(oldUserPerm)
        1 * userPermissionRepository.save(newUserPerm)
    }

    def "should insert admin permission if none exist"() {
        given:
        def seasonId = 999
        def oldAdmin = new UserDao(userId: 123, username: "old-admin")
        def newAdmin = new UserDao(userId: 456, username: "new-admin")

        def writePermission = new PermissionDao(
                resourceType:  ResourceType.SEASON,
                permissionType:  PermissionType.WRITE,
                resourceId:  seasonId
        )
        def newUserPerm = new UserPermissionDao(
                userDao: newAdmin,
                permissionDao: writePermission
        )

        when:
        defaultPermissionService.swapSeasonAdmins(seasonId, oldAdmin, newAdmin)

        then:
        1 * permissionRepository.findByResourceIdAndResourceTypeAndPermissionType(
                seasonId, ResourceType.SEASON, PermissionType.WRITE
        ) >> Optional.empty()
        1 * permissionRepository.save(writePermission)
        1 * userPermissionRepository.save(newUserPerm)
    }
}

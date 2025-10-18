package com.sgg.users

import com.sgg.users.authz.DefaultPermissionService
import com.sgg.users.authz.PermissionDao
import com.sgg.users.authz.PermissionRepository
import com.sgg.users.authz.PermissionType
import com.sgg.users.authz.ResourceType
import com.sgg.users.authz.UserPermissionDao
import com.sgg.users.authz.UserPermissionRepository
import spock.lang.Specification

class DefaultUserServiceSpec extends Specification {

    PermissionRepository permissionRepository = Mock()
    UserPermissionRepository userPermissionRepository = Mock()

    DefaultPermissionService defaultPermissionService = new DefaultPermissionService(
            permissionRepository,
            userPermissionRepository
    )

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

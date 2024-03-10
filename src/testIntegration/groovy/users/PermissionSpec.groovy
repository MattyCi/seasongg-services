package users

import com.sgg.users.UserDao
import com.sgg.users.UserRepository
import com.sgg.users.authz.PermissionDao
import com.sgg.users.authz.PermissionRepository
import com.sgg.users.authz.PermissionType
import com.sgg.users.authz.ResourceType
import com.sgg.users.authz.UserPermissionDao
import com.sgg.users.authz.UserPermissionRepository
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.hibernate.exception.ConstraintViolationException
import spock.lang.Shared
import spock.lang.Specification

import javax.persistence.PersistenceException
import java.time.OffsetDateTime

@MicronautTest
class PermissionSpec extends Specification {

    @Inject
    @Shared
    PermissionRepository permissionRepository

    @Inject
    @Shared
    UserRepository userRepository

    @Inject
    @Shared
    UserPermissionRepository userPermissionRepository

    @Shared
    UserDao userDao

    @Shared
    PermissionDao permissionDao

    def setupSpec() {
        userDao = userRepository.save(
                new UserDao(
                        username: "matt",
                        password: "sgg-test-123",
                        registrationTime: OffsetDateTime.now()
                )
        )
        permissionDao = permissionRepository.save(
                new PermissionDao(
                        permissionType: PermissionType.WRITE,
                        resourceId: 123L,
                        resourceType: ResourceType.ROUND
                )
        )
    }

    def 'prevent duplicate permission records in the database'() {
        given:
        final duplicate = new PermissionDao(
                permissionType: PermissionType.WRITE,
                resourceId: 123L,
                resourceType: ResourceType.ROUND
        )

        when:
        permissionRepository.save(duplicate)

        then:
        def e = thrown(PersistenceException)
        e.cause instanceof ConstraintViolationException
    }

    def 'throw exceptions when required columns are null'() {
        when:
        permissionRepository.save(permissionDao)

        then:
        def e = thrown(PersistenceException)
        e.cause instanceof ConstraintViolationException

        where:
        permissionDao << [
                new PermissionDao(),
                new PermissionDao(
                        resourceType: ResourceType.ROUND,
                        permissionType: PermissionType.WRITE
                ),
                new PermissionDao(
                        resourceType: ResourceType.ROUND,
                        resourceId: 123L
                ),
                new PermissionDao(
                        resourceId: 123L,
                        permissionType: PermissionType.READ
                )
        ]
    }

    def 'prevent duplicate user permission records in the database'() {
        given:
        final userPermissionDao = new UserPermissionDao(
                userDao: userDao,
                permissionDao: permissionDao
        )
        final duplicate = new UserPermissionDao(
                userDao: userDao,
                permissionDao: permissionDao
        )

        when:
        userPermissionRepository.save(userPermissionDao)
        userPermissionRepository.save(duplicate)

        then:
        def e = thrown(PersistenceException)
        e.cause instanceof ConstraintViolationException
    }
}
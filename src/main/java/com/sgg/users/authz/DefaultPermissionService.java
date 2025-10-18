package com.sgg.users.authz;

import com.sgg.users.UserDao;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Optional;

@Slf4j
@Singleton
@AllArgsConstructor(onConstructor_ = @Inject)
public class DefaultPermissionService implements PermissionService {
    private PermissionRepository permissionRepository;
    private UserPermissionRepository userPermissionRepository;

    public Optional<PermissionDao> findSeasonPermission(Long seasonId) {
        return permissionRepository.findByResourceIdAndResourceTypeAndPermissionType(seasonId, ResourceType.SEASON,
                PermissionType.WRITE);
    }

    public Optional<UserPermissionDao> findUserPermission(UserDao user, PermissionDao permission) {
        return userPermissionRepository.findByUserDaoAndPermissionDao(user, permission);
    }

    public void insertSeasonAdminPermission(Long resourceId, UserDao user) {
        val permission = PermissionDao.builder()
                .permissionType(PermissionType.WRITE)
                .resourceId(resourceId)
                .resourceType(ResourceType.SEASON)
                .build();
        val userPermission = UserPermissionDao.builder()
                .permissionDao(permission)
                .userDao(user)
                .build();
        permissionRepository.save(permission);
        userPermissionRepository.save(userPermission);
    }

    public void swapSeasonAdmins(Long seasonId, UserDao oldAdmin, UserDao newAdmin) {
        val adminPermission = permissionRepository.findByResourceIdAndResourceTypeAndPermissionType(
                seasonId, ResourceType.SEASON, PermissionType.WRITE);
        if (adminPermission.isEmpty()) {
            log.warn("No admin permission was found for season {}. Adding permission for new admin {}", seasonId,
                    newAdmin.getUserId());
            insertSeasonAdminPermission(seasonId, newAdmin);
        } else {
            val oldUserPerm = userPermissionRepository.findByUserDaoAndPermissionDao(oldAdmin, adminPermission.get());
            oldUserPerm.ifPresentOrElse(userPermissionRepository::delete, () -> log.warn("No user permission existed " +
                    "for season {}, permId {}", seasonId, adminPermission.get().getPermId()));
            val newUserPerm = UserPermissionDao.builder()
                    .permissionDao(adminPermission.get())
                    .userDao(newAdmin)
                    .build();
            userPermissionRepository.save(newUserPerm);
        }
    }
}

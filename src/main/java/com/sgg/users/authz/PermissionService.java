package com.sgg.users.authz;

import com.sgg.users.UserDao;

import java.util.Optional;

public interface PermissionService {
    Optional<PermissionDao> findSeasonPermission(Long seasonId);

    Optional<UserPermissionDao> findUserPermission(UserDao user, PermissionDao permission);

    void insertSeasonAdminPermission(Long resourceId, UserDao user);

    void swapSeasonAdmins(Long seasonId, UserDao oldAdmin, UserDao newAdmin);
}

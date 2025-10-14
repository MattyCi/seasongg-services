package com.sgg.users.authz;

import com.sgg.users.UserDao;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public
interface UserPermissionRepository extends CrudRepository<UserPermissionDao, Integer> {
    Optional<UserPermissionDao> findByUserDaoAndPermissionDao(UserDao userDao, PermissionDao permissionDao);
}

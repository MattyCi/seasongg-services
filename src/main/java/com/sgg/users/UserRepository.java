package com.sgg.users;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public
interface UserRepository extends CrudRepository<UserDao, Long> {

    Optional<UserDao> findByUsernameIgnoreCase(String username);

    @Query("FROM UserDao u LEFT JOIN FETCH u.userPermissionEntities up WHERE LOWER(u.username) = LOWER(:username)")
    Optional<UserDao> findByUsernameIgnoreCaseWithUserPermissions(String username);
}

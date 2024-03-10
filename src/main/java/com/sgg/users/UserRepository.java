package com.sgg.users;

import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public
interface UserRepository extends CrudRepository<UserDao, Long> {

    @Join(value = "userPermissionEntities", type = Join.Type.FETCH)
    Optional<UserDao> findByUsernameIgnoreCase(String username);

}

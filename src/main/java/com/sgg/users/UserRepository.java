package com.sgg.users;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
interface UserRepository extends CrudRepository<UserDao, Long> {

    Optional<UserDao> findByUsernameIgnoreCase(String username);

}

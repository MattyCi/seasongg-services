package com.sgg.users.auth;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
interface UserPermissionRepository extends CrudRepository<UserPermissionDao, Integer> {

}

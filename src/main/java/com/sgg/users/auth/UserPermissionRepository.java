package com.sgg.users.auth;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
public
interface UserPermissionRepository extends CrudRepository<UserPermissionDao, Integer> {

}

package com.sgg.users.authz;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
public
interface UserPermissionRepository extends CrudRepository<UserPermissionDao, Integer> {

}

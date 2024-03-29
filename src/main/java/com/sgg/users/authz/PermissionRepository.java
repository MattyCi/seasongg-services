package com.sgg.users.authz;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
public
interface PermissionRepository extends CrudRepository<PermissionDao, Integer> {

}

package com.sgg.users;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
interface PermissionRepository extends CrudRepository<Permission, Integer> {

}

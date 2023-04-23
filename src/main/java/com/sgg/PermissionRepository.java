package com.sgg;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
public interface PermissionRepository extends CrudRepository<Permission, Integer> {

}

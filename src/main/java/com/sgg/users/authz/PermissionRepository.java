package com.sgg.users.authz;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public
interface PermissionRepository extends CrudRepository<PermissionDao, Integer> {

    Optional<PermissionDao> findByResourceIdAndResourceTypeAndPermissionType(
            Long resourceId,
            ResourceType resourceType,
            PermissionType permissionType
    );
}

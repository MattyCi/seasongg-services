package com.sgg.users.authz;

import com.sgg.users.model.PermissionDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "jsr330")
public interface PermissionMapper {
    PermissionDto permissionToPermissionDto(PermissionDao permissionDao);
    PermissionDao permissionToPermissionDao(PermissionDto permissionDto);
}

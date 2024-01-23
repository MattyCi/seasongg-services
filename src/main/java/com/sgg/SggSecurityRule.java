package com.sgg;

import com.sgg.users.auth.PermissionType;
import com.sgg.users.auth.ResourceType;

public @interface SggSecurityRule {

    /**
     * The type of resource the authorization check is happening against
     */
    ResourceType resourceType();

    /**
     * The name of the parameter in the controller method which contains the
     * resource ID this permission is required for
     */
    String resourceIdName();

    /**
     * The permission required for the given resource
     */
    PermissionType permissionType();

}

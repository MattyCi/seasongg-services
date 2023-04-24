package com.sgg;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ApplicationDataBootstrap implements ApplicationEventListener<ServerStartupEvent> {

    /*@Inject
    ReguserRepository reguserRepository;

    @Inject
    PermissionRepository permissionRepository;

    @Inject
    UserPermissionRepository userPermissionRepository;*/

    @Override
    public void onApplicationEvent(ServerStartupEvent event) {

        /*if (reguserRepository.findByUsernameIgnoreCase("matt").isEmpty()) {
            Reguser reguser = new Reguser("matt", "test");
            reguserRepository.save(reguser);

            Permission permission;
            permission = new Permission();
            permission.setPermValue("season:*:123");
            permissionRepository.save(permission);

            UserPermission userPermission = new UserPermission();
            userPermission.setPermission(permission);
            userPermission.setReguser(reguser);
            userPermissionRepository.save(userPermission);

        }
        */
    }
}

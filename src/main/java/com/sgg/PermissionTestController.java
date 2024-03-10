package com.sgg;

import com.sgg.users.authz.SggSecurityRule;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import java.security.Principal;

import static com.sgg.users.authz.PermissionType.WRITE;
import static com.sgg.users.authz.ResourceType.SEASON;

// TODO: move this to a test
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("test")
public class PermissionTestController {

    @Produces(MediaType.TEXT_PLAIN)
    @Get // <3>
    public String index(Principal principal) {  // <4>
        return principal.getName();
    }

    @Produces(MediaType.TEXT_PLAIN)
    @Get("authcheck/{seasonId}")
    @SggSecurityRule(resourceType = SEASON, permissionType = WRITE, resourceIdName = "seasonId")
    public String authCheck(@PathVariable String seasonId) {
        return "HELLO, AUTHENTICATION CHECK WORKED!!!";
    }

}

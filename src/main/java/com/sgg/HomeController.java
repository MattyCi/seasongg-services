package com.sgg;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import java.security.Principal;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("test")
public class HomeController {

    @Produces(MediaType.TEXT_PLAIN)
    @Get // <3>
    public String index(Principal principal) {  // <4>
        return principal.getName();
    }

    @Produces(MediaType.TEXT_PLAIN)
    @Get("authcheck/{seasonId}")
    @CustomSecurityRuleAnnotation(resourceIdName = "seasonId", permission = "WRITE")
    public String authCheck(@PathVariable String seasonId) {
        return "HELLO, AUTHENTICATION CHECK WORKED!!!";
    }

}

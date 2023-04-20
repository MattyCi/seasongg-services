package example.micronaut;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import java.security.Principal;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("public")
public class PublicController {

    @Produces(MediaType.TEXT_PLAIN)
    @Get
    public String index() {
        return "PUBLIC!!!";
    }

}

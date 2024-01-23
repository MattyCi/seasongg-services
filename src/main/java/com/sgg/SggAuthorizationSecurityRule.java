package com.sgg;

import com.sgg.users.auth.PermissionType;
import com.sgg.users.auth.ResourceType;
import com.sgg.users.model.PermissionDto;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecuredAnnotationRule;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.rules.SecurityRuleResult;
import io.micronaut.web.router.MethodBasedRouteMatch;
import io.micronaut.web.router.RouteMatch;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Singleton
@Slf4j
public class SggAuthorizationSecurityRule implements SecurityRule {

    public static final Integer ORDER = SecuredAnnotationRule.ORDER - 100;

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public Publisher<SecurityRuleResult> check(HttpRequest<?> request, RouteMatch<?> routeMatch,
                                               Authentication authentication) {
        if (isAuthorizationRequired(routeMatch)) {

            val annotation = Optional.ofNullable(routeMatch.getAnnotation(SggSecurityRule.class));

            if (annotation.isPresent()) {

                if (annotation.get().stringValue("resourceIdName").isEmpty() ||
                        annotation.get().enumValue("permissionType", PermissionType.class).isEmpty() ||
                        annotation.get().enumValue("resourceType", ResourceType.class).isEmpty()) {
                    log.error("Authorization routing has been misconfigured");
                    throw new RuntimeException("An unexpected error occurred.");
                }

                val resourceIdName = annotation.get().stringValue("resourceIdName").get();
                val permissionType = annotation.get().enumValue("permissionType", PermissionType.class).get();
                val resourceType = annotation.get().enumValue("resourceType", ResourceType.class).get();
                val resourceId = routeMatch.getVariableValues().get(resourceIdName).toString();


                if (isAllowed(authentication, resourceId, resourceIdName, permissionType, resourceType))
                    return Mono.just(SecurityRuleResult.ALLOWED);
            }

        }
        return Mono.just(SecurityRuleResult.UNKNOWN);
    }

    private boolean isAllowed(Authentication authentication, String resourceIdName, String resourceId,
                              PermissionType permissionType,ResourceType resourceType) {

        // TODO: what if authentication is null? ie. bad token? or not logged in?

        var claims = authentication.getAttributes().get("claims");




        return false;
    }



    private boolean isAuthorizationRequired(RouteMatch<?> routeMatch) {
        if (routeMatch instanceof MethodBasedRouteMatch<?, ?> methodBasedRouteMatch) {
            return methodBasedRouteMatch.hasAnnotation(SggSecurityRule.class);
        }
        return false;
    }
}


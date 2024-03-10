package com.sgg;

import com.sgg.users.auth.PermissionType;
import com.sgg.users.auth.ResourceType;
import io.micronaut.core.annotation.NonNull;
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

import java.util.Map;
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
        if (!isAuthorizationRequired(routeMatch))
            return Mono.just(SecurityRuleResult.UNKNOWN);

        if (authentication == null)
            return Mono.just(SecurityRuleResult.REJECTED);

        val annotation = Optional.ofNullable(routeMatch.getAnnotation(SggSecurityRule.class));

        if (annotation.isEmpty() || annotation.get().stringValue("resourceIdName").isEmpty() ||
                annotation.get().enumValue("permissionType", PermissionType.class).isEmpty() ||
                annotation.get().enumValue("resourceType", ResourceType.class).isEmpty()) {
            log.error("Authorization routing has been misconfigured");
            throw new RuntimeException("An unexpected error occurred.");
        }

        val resourceIdName = annotation.get().stringValue("resourceIdName").get();
        val permissionType = annotation.get().enumValue("permissionType", PermissionType.class).get();
        val resourceType = annotation.get().enumValue("resourceType", ResourceType.class).get();
        val resourceId = routeMatch.getVariableValues().get(resourceIdName).toString();

        if (isAllowed(authentication, resourceId, permissionType, resourceType.toString())) {
            return Mono.just(SecurityRuleResult.UNKNOWN);
        } else {
            log.debug("user {} tried accessing the following resource but was denied: {}:{}:{}",
                    authentication.getName(), resourceType, resourceId, permissionType);
            return Mono.just(SecurityRuleResult.REJECTED);
        }
    }

    private boolean isAuthorizationRequired(RouteMatch<?> routeMatch) {
        if (routeMatch instanceof MethodBasedRouteMatch<?, ?> methodBasedRouteMatch) {
            return methodBasedRouteMatch.hasAnnotation(SggSecurityRule.class);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean isAllowed(@NonNull Authentication authentication, String resourceId,
                              PermissionType permissionType, String resourceType) {
        val claims = (Map<String, String>) authentication.getAttributes().get("claims");
        val matchedPerm = claims.get(String.format("%s:%s", resourceType, resourceId));
        return matchedPerm != null && matchedPerm.equals(permissionType.toString());
    }
}


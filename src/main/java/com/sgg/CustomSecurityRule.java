package com.sgg;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecuredAnnotationRule;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.rules.SecurityRuleResult;
import io.micronaut.web.router.MethodBasedRouteMatch;
import io.micronaut.web.router.RouteMatch;
import jakarta.inject.Singleton;
import lombok.val;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Singleton
public class CustomSecurityRule implements SecurityRule<HttpRequest<?>> {

    public static final Integer ORDER = SecuredAnnotationRule.ORDER - 100;

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public Publisher<SecurityRuleResult> check(HttpRequest<?> request, @Nullable Authentication authentication) {

        System.out.println("in check()");
        RouteMatch<?> routeMatch = request.getAttribute(HttpAttributes.ROUTE_MATCH, RouteMatch.class).orElse(null);

        if (routeMatch instanceof MethodBasedRouteMatch) {
            val methodBasedRouteMatch = (MethodBasedRouteMatch<?, ?>) routeMatch;

            if (methodBasedRouteMatch.hasAnnotation(CustomSecurityRuleAnnotation.class)) {
                val requiredPermissionAnnotation = Optional.ofNullable(methodBasedRouteMatch.getAnnotation(CustomSecurityRuleAnnotation.class));

                if (requiredPermissionAnnotation.isPresent()) {

                    // Get parameters from annotation on method
                    Optional<String> resourceIdName = requiredPermissionAnnotation.get().stringValue("resourceIdName");
                    Optional<String> permission = requiredPermissionAnnotation.get().stringValue("permission");

                    if (permission.isPresent() && resourceIdName.isPresent()) {

                        System.out.println("\nAUTH ANNOTATION DATA:");

                        System.out.println("resourceIdName: " + resourceIdName.get());
                        System.out.println("permission: " + permission.get());

                        String resourceId = methodBasedRouteMatch.getVariableValues().get(resourceIdName.get()).toString();
                        System.out.println("\nPROVIDED DATA:");
                        System.out.println("resourceId: " + resourceId);

                        System.out.println("\nPRINTING AUTH DETAILS:");
                        System.out.println(authentication.getName());
                        System.out.println(authentication.getRoles());

                        System.out.println(authentication.getAttributes().get("authorities"));

                        // TODO: add logic to check for perm!

                        return Mono.just(SecurityRuleResult.ALLOWED);

                    }

                }

            }
        }

        System.out.println("UNKNOWN!");
        return Mono.just(SecurityRuleResult.UNKNOWN);

    }

}


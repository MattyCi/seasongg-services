package com.sgg;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.hibernate.Hibernate;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.*;

@Singleton
public class AuthenticationProviderUserPassword implements AuthenticationProvider {

    //@Inject
    //ReguserRepository reguserRepository;

    @Override
    public Publisher<AuthenticationResponse> authenticate(@Nullable HttpRequest<?> httpRequest,
                                                          AuthenticationRequest<?, ?> authenticationRequest) {

        /*System.out.println("WE ARE IN THIS PROVIDER!!!");

        Optional<Reguser> user = reguserRepository.findByUsernameIgnoreCase(
                authenticationRequest.getIdentity().toString());

        user.ifPresent(value -> System.out.println("AUTHENTICATING REGUSER: " + value.getUsername()));

        if (user.isPresent() && user.get().getPassword().equals(authenticationRequest.getSecret().toString())) {

            Map<String, Object> attributes = new HashMap<>();

            Reguser reguser = user.get();

            Hibernate.initialize(reguser.getUserPermissions());
            List<UserPermission> perms = reguser.getUserPermissions();

            ArrayList<String> permVals = new ArrayList<>();

            perms.forEach(
                    (p) -> {
                        System.out.println(p.getPermission().getPermValue());
                        permVals.add(p.getPermission().getPermValue());
                    }
            );

            attributes.put("authorities", permVals);

            return Mono.just(
                    AuthenticationResponse.success(authenticationRequest.getIdentity().toString(), attributes)
            );
        } else {
            return Mono.error(AuthenticationResponse.exception());
        }*/

        return Mono.just(
                AuthenticationResponse.success(authenticationRequest.getIdentity().toString(), new HashMap<>())
        );

    }
}

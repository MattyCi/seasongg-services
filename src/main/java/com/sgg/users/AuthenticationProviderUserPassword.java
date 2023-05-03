package com.sgg.users;

import com.sgg.users.security.PasswordEncoder;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationFailed;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;

@Slf4j
@Singleton
@AllArgsConstructor(onConstructor_ = @Inject)
public class AuthenticationProviderUserPassword implements AuthenticationProvider {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    private static final String ERR_USERNAME_OR_PASSWORD_FAILED = "The username or password provided didn't " +
            "match our records. Please try again.";

    @Override
    public Mono<AuthenticationResponse> authenticate(@Nullable HttpRequest<?> httpRequest,
                                                     AuthenticationRequest<?, ?> authenticationRequest) {

        log.debug("login attempt for: {}", authenticationRequest.getIdentity());

        return Mono.fromCallable(() -> validateLoginFromDataSource(authenticationRequest))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private AuthenticationResponse validateLoginFromDataSource(AuthenticationRequest<?, ?> authenticationRequest) {
        var user = userRepository.findByUsernameIgnoreCase(authenticationRequest.getIdentity().toString());

        if (user.isEmpty() || !passwordMatches(authenticationRequest.getSecret().toString(), user.get().getPassword())) {
            log.info("failed login attempt for user {}", authenticationRequest.getIdentity());
            return new AuthenticationFailed(ERR_USERNAME_OR_PASSWORD_FAILED);
        }

        // TODO: provide authorities instead of empty list
        return AuthenticationResponse.success("matt", new ArrayList<>());
    }

    private boolean passwordMatches(String givenPassword, String storedPassword) {
        return passwordEncoder.matches(givenPassword, storedPassword);
    }

}

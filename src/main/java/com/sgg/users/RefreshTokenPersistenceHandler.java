package com.sgg.users;

import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.event.RefreshTokenGeneratedEvent;
import io.micronaut.security.token.refresh.RefreshTokenPersistence;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Slf4j
@Singleton
@AllArgsConstructor(onConstructor_ = @Inject)
public class RefreshTokenPersistenceHandler implements RefreshTokenPersistence {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    public void persistToken(RefreshTokenGeneratedEvent event) {
        if (eventDataMissing(event)) {
            log.error("unexpected error - refresh token event data missing!");
            return;
        }

        userRepository.findByUsernameIgnoreCase(event.getAuthentication().getName())
                .ifPresentOrElse(
                        user -> persistTokenToUser(event.getRefreshToken(), user),
                        () -> log.error("unexpected error - user {} not found for generated refresh token",
                                event.getAuthentication().getName())
                );
    }

    private boolean eventDataMissing(RefreshTokenGeneratedEvent event) {
        return event == null ||
                event.getRefreshToken() == null ||
                event.getAuthentication() == null ||
                event.getAuthentication().getName() == null;
    }

    private void persistTokenToUser(String refreshToken, UserDao user) {
        log.info("persisting refresh token for user {}", user.getUserId());

        refreshTokenRepository.save(RefreshTokenDao.builder()
                .refreshToken(refreshToken)
                .userDao(user)
                .revoked(false)
                .build());
    }

    @Override
    public Publisher<Authentication> getAuthentication(String refreshToken) {
        // TODO: finish implementation
        return Mono.just(Authentication.build("TODO"));
    }

}

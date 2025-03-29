package com.sgg.users.authn;

import com.sgg.users.UserDao;
import com.sgg.users.UserRepository;
import com.sgg.users.security.RefreshTokenService;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.errors.OauthErrorResponseException;
import io.micronaut.security.token.event.RefreshTokenGeneratedEvent;
import io.micronaut.security.token.refresh.RefreshTokenPersistence;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static io.micronaut.security.errors.IssuingAnAccessTokenErrorCode.INVALID_GRANT;

@Slf4j
@Singleton
@AllArgsConstructor(onConstructor_ = @Inject)
public class RefreshTokenPersistenceHandler implements RefreshTokenPersistence {

    private final RefreshTokenService refreshTokenService;
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
        log.debug("persisting refresh token for user {}", user.getUserId());

        refreshTokenService.persistRefreshToken(RefreshTokenDao.builder()
                .refreshToken(refreshToken)
                .userDao(user)
                .revoked(false)
                .build());
    }

    @Override
    public Publisher<Authentication> getAuthentication(String refreshToken) {
        return Mono.fromCallable(
                () -> getAuthenticationFromDataSource(refreshToken)
        ).onErrorMap(t -> t).subscribeOn(Schedulers.boundedElastic());
    }

    private Authentication getAuthenticationFromDataSource(String refreshToken) throws OauthErrorResponseException {
        return refreshTokenService.findRefreshToken(refreshToken)
                .map(this::verifyTokenIsNotRevoked)
                .orElseThrow(() -> new OauthErrorResponseException(INVALID_GRANT, "refresh token not found", null));
    }

    private Authentication verifyTokenIsNotRevoked(RefreshTokenDao token) throws OauthErrorResponseException {
        if (token.getRevoked())
            throw new OauthErrorResponseException(INVALID_GRANT, "refresh token revoked", null);

        return Authentication.build(token.getUserDao().getUsername());
    }

}

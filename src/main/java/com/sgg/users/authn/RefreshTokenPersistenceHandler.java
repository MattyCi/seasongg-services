package com.sgg.users.authn;

import com.sgg.users.UserDao;
import com.sgg.users.UserRepository;
import com.sgg.users.authz.PermissionMapper;
import com.sgg.users.authz.UserPermissionDao;
import com.sgg.users.model.PermissionDto;
import com.sgg.users.security.RefreshTokenService;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.errors.OauthErrorResponseException;
import io.micronaut.security.token.event.RefreshTokenGeneratedEvent;
import io.micronaut.security.token.refresh.RefreshTokenPersistence;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.stream.Collectors;

import static io.micronaut.security.errors.IssuingAnAccessTokenErrorCode.INVALID_GRANT;

@Slf4j
@Singleton
@AllArgsConstructor(onConstructor_ = @Inject)
public class RefreshTokenPersistenceHandler implements RefreshTokenPersistence {

    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    PermissionMapper permissionMapper;

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
        val token = refreshTokenService.findRefreshToken(refreshToken)
                .orElseThrow(() -> new OauthErrorResponseException(INVALID_GRANT, "refresh token not found", null));
        if (token.getRevoked())
            throw new OauthErrorResponseException(INVALID_GRANT, "refresh token revoked", null);
        return buildAuthWithClaims(token);
    }

    private Authentication buildAuthWithClaims(RefreshTokenDao token) throws OauthErrorResponseException {
        val permissions = token.getUserDao().getUserPermissionEntities().stream()
                .map(UserPermissionDao::getPermissionDao)
                .map(dao -> permissionMapper.permissionToPermissionDto(dao))
                .collect(Collectors.toMap(
                        PermissionDto::formatPermission,
                        PermissionDto::getPermissionType
                ));
        val attributes = Map.of("claims", permissions, "userId", token.getUserDao().getUserId());
        return Authentication.build(token.getUserDao().getUsername(), attributes);
    }
}

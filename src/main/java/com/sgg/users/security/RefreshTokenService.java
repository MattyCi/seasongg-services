package com.sgg.users.security;

import com.sgg.users.authn.RefreshTokenDao;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Singleton
@Slf4j
@AllArgsConstructor(onConstructor_ = @Inject)
public class RefreshTokenService {

    RefreshTokenRepository refreshTokenRepository;

    public Optional<RefreshTokenDao> findRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken);
    }

    public void persistRefreshToken(RefreshTokenDao refreshTokenDao) {
        refreshTokenRepository.save(refreshTokenDao);
    }

    public long count() {
        return refreshTokenRepository.count();
    }

    public void deleteAll() {
        log.warn("deleting all refresh tokens!");
        refreshTokenRepository.deleteAll();
    }

    public void revokeAll() {
        refreshTokenRepository.findAll().forEach(
                (it) -> {
                    it.setRevoked(true);
                    refreshTokenRepository.update(it);
                }
        );
    }

}

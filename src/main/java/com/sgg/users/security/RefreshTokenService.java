package com.sgg.users.security;

import com.sgg.common.SggException;
import com.sgg.users.RefreshTokenDao;
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

    public void revokeRefreshToken(long id) {
        refreshTokenRepository.findById(id).ifPresentOrElse(
                (td) -> {
                    td.setRevoked(true);
                    refreshTokenRepository.update(td);
                },
                () -> { throw new SggException("Token not found."); }
        );
    }

}

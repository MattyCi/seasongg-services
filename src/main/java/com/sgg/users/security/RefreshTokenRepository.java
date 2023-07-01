package com.sgg.users.security;

import com.sgg.users.RefreshTokenDao;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
interface RefreshTokenRepository extends CrudRepository<RefreshTokenDao, Long> {

    Optional<RefreshTokenDao> findByRefreshToken(String refreshToken);

}

package com.sgg.users;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public
interface RefreshTokenRepository extends CrudRepository<RefreshTokenDao, Long> {

    Optional<RefreshTokenDao> findByRefreshToken(String refreshToken);

}

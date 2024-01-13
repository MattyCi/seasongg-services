package com.sgg.users.security;

import com.sgg.users.auth.RefreshTokenDao;
import com.sgg.users.UserDao;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@Repository
interface RefreshTokenRepository extends CrudRepository<RefreshTokenDao, Long> {

    Optional<RefreshTokenDao> findByRefreshToken(String refreshToken);

    List<RefreshTokenDao> findByUserDao(UserDao userDao);

}

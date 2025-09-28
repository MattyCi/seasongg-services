package com.sgg.users.security;

import com.sgg.users.authn.RefreshTokenDao;
import com.sgg.users.UserDao;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@Repository
interface RefreshTokenRepository extends CrudRepository<RefreshTokenDao, Long> {

    @Query("SELECT r FROM RefreshTokenDao r JOIN FETCH r.userDao u LEFT JOIN FETCH u.userPermissionEntities WHERE r.refreshToken = :refreshToken")
    Optional<RefreshTokenDao> findByRefreshToken(String refreshToken);

    List<RefreshTokenDao> findByUserDao(UserDao userDao);

}

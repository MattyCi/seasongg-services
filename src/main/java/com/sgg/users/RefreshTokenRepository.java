package com.sgg.users;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
interface RefreshTokenRepository extends CrudRepository<RefreshTokenDao, Long> {

}

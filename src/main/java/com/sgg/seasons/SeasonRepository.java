package com.sgg.seasons;

import com.sgg.seasons.model.SeasonDao;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public
interface SeasonRepository extends CrudRepository<SeasonDao, Long> {
    Optional<SeasonDao> findByNameIgnoreCase(String name);
}

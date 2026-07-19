package com.sgg.seasons;

import com.sgg.seasons.model.SeasonDao;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.Optional;

@Repository
public
interface SeasonRepository extends PageableRepository<SeasonDao, Long> {
    Optional<SeasonDao> findByNameIgnoreCase(String name);
}

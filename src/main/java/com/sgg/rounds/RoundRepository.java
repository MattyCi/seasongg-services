package com.sgg.rounds;

import com.sgg.rounds.model.RoundDao;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public
interface RoundRepository extends CrudRepository<RoundDao, Long> {
    @Join(value = "roundResults", type = Join.Type.FETCH)
    Optional<RoundDao> findById(@NonNull Long id);
}

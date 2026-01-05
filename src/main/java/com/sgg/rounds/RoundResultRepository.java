package com.sgg.rounds;

import com.sgg.rounds.model.RoundResultDao;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
public
interface RoundResultRepository extends CrudRepository<RoundResultDao, Long> {

}

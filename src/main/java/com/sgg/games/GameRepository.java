package com.sgg.games;

import com.sgg.games.model.GameDao;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
public interface GameRepository extends CrudRepository<GameDao, Long> {

}

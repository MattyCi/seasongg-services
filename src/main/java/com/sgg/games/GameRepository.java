package com.sgg.games;

import com.sgg.games.model.GameDao;
import io.micronaut.data.repository.CrudRepository;

public interface GameRepository extends CrudRepository<GameDao, Integer> {

}

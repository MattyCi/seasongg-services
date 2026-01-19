package com.sgg

import com.sgg.games.model.GameDao
import com.sgg.rounds.model.RoundDao
import com.sgg.rounds.model.RoundResultDao
import com.sgg.seasons.model.SeasonDao
import com.sgg.users.UserDao

class DaoFixtures {

    static SeasonDao validSeason() {
        return SeasonDao.builder()
                .seasonId(1)
                .name("season-test")
                .status("ACTIVE")
                .rounds([])
                .standings([])
                .game(GameDao.builder().gameId(1).name("Catan").build())
                .build()
    }

    static RoundDao roundWith(List<RoundResultDao> results) {
        return RoundDao.builder()
                .roundResults(results)
                .build()
    }

    static RoundResultDao roundResult(int points, UserDao player) {
        RoundResultDao.builder()
                .user(player)
                .points(points)
                .build()
    }

    static UserDao matty() {
        return UserDao.builder()
                .userId(31)
                .username("matty")
                .build()
    }

    static UserDao aaron() {
        return UserDao.builder()
                .userId(5)
                .username("aaron")
                .build()
    }

    static UserDao mistalegit() {
        return UserDao.builder()
                .userId(7)
                .username("mistalegit")
                .build()
    }

    static UserDao dan() {
        return UserDao.builder()
                .userId(77)
                .username("dan")
                .build()
    }
}

package com.sgg

import com.sgg.games.model.GameDto
import com.sgg.rounds.model.RoundDto
import com.sgg.rounds.model.RoundResultDto
import com.sgg.seasons.model.SeasonDto
import com.sgg.users.model.UserDto

class DtoFixtures {

    static SeasonDto validSeason() {
        return SeasonDto.builder()
                .seasonId(1)
                .name("season-test")
                .game(GameDto.builder().gameId(1).name("Catan").build())
                .build()
    }

    static RoundDto roundWith(List<RoundResultDto> results) {
        return RoundDto.builder()
                .roundResults(results)
                .build()
    }

    static RoundResultDto roundResult(int points, UserDto player) {
        RoundResultDto.builder()
                .user(player)
                .points(points)
                .build()
    }

    static UserDto matty() {
        return UserDto.builder()
                .userId(31)
                .username("matty")
                .build()
    }

    static UserDto aaron() {
        return UserDto.builder()
                .userId(5)
                .username("aaron")
                .build()
    }

    static UserDto mistalegit() {
        return UserDto.builder()
                .userId(7)
                .username("mistalegit")
                .build()
    }

    static UserDto dan() {
        return UserDto.builder()
                .userId(77)
                .username("dan")
                .build()
    }
}

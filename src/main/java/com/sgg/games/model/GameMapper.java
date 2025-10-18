package com.sgg.games.model;

import org.mapstruct.Mapper;

@Mapper(componentModel = "jsr330")
public interface GameMapper {
    GameDto toGameDto(GameDao gameDao);
    GameDao toGameDao(GameDto gameDto);
}

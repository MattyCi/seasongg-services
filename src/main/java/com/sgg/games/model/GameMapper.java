package com.sgg.games.model;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "jsr330", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GameMapper {
    GameDto toGameDto(GameDao gameDao);
    GameDao toGameDao(GameDto gameDto);
}

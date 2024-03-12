package com.sgg.seasons.model;

import org.mapstruct.Mapper;

@Mapper(componentModel = "jsr330")
public interface SeasonMapper {
    SeasonDto toSeasonDto(SeasonDao seasonDao);
    SeasonDao toSeasonDao(SeasonDto seasonDto);
}

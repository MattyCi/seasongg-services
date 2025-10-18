package com.sgg.seasons.model;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "jsr330", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SeasonMapper {
    SeasonDto toSeasonDto(SeasonDao seasonDao);
    SeasonDao toSeasonDao(SeasonDto seasonDto);
}

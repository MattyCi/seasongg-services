package com.sgg.seasons.model;

import com.sgg.rounds.model.RoundDao;
import com.sgg.rounds.model.RoundDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "jsr330", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SeasonMapper {
    SeasonDto toSeasonDto(SeasonDao seasonDao);
    SeasonDao toSeasonDao(SeasonDto seasonDto);

    @Mapping(target = "season", ignore = true)
    RoundDto roundDaoToRoundDto(RoundDao roundDao);
}

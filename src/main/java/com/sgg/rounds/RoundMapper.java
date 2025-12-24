package com.sgg.rounds;

import com.sgg.rounds.model.RoundDao;
import com.sgg.rounds.model.RoundDto;
import com.sgg.rounds.model.RoundResultDao;
import com.sgg.rounds.model.RoundResultDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "jsr330", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoundMapper {
    @Mapping(target = "season", ignore = true)
    RoundDto toRoundDto(RoundDao roundDao);
    RoundDao toRoundDao(RoundDto roundDto);

    @Mapping(target = "round.roundResults", ignore = true)
    RoundResultDao toRoundResultDao(RoundResultDto roundResultDto);
}

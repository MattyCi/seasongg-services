package com.sgg.games.model;

import com.sgg.seasons.model.SeasonDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameDto {
    @NotNull
    private Long gameId;

    @NotNull
    // TODO: how to validate name?
    private String name;

    private List<SeasonDto> seasons;
}

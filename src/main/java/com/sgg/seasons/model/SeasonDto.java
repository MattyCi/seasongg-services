package com.sgg.seasons.model;

import com.sgg.games.model.GameDto;
import com.sgg.rounds.model.RoundDto;
import com.sgg.users.model.UserDto;
import io.micronaut.core.annotation.Introspected;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Introspected
@ValidSeason
public class SeasonDto {
    private Long seasonId;

    @NotBlank(message = "{season.name.NotBlank}")
    @Size(min = 3, max = 56, message = "{season.name.Length}")
    @Pattern(regexp = "^[a-zA-Z0-9 |\\-.+',_?~!@&$]*$", message = "{season.name.Pattern}")
    private String name;

    private OffsetDateTime startDate;

    private OffsetDateTime endDate;

    @NotNull(message = "{season.creator.NotNull}") // TODO: should this be specified here ???
    private UserDto creator;

    @NotNull(message = "{season.status.NotNull}")
    private SeasonStatus status;

    private List<RoundDto> rounds;

    @NotNull(message = "{season.game.NotNull}")
    @Valid
    private GameDto game;

    // TODO: add season standings

    // TODO: add configurable season scoring options ie. averaged, Best Of N, etc.
}

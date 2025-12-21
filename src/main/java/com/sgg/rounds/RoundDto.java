package com.sgg.rounds;

import com.sgg.seasons.model.SeasonDto;
import com.sgg.users.model.UserDto;
import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Introspected
public class RoundDto {
    Long roundId;

    @NotNull(message = "{round.roundDate.NotNull}")
    private OffsetDateTime roundDate;

    @NotNull(message = "{round.roundResults.NotNull}")
    @Size(min = 2, message = "{round.roundResults.Size}")
    private List<RoundResultDto> roundResults;

    @NotNull(message = "{round.season.NotNull}")
    private SeasonDto season;

    @NotNull(message = "{round.creator.NotNull}")
    private UserDto creator;
}

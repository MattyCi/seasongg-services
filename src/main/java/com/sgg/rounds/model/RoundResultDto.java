package com.sgg.rounds.model;

import com.sgg.users.model.UserDto;
import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Introspected
public class RoundResultDto {
    private Long roundResultsId;

    @NotNull(message = "{round.result.place.NotNull}")
    @Min(value = 1, message = "{round.result.place.Min}")
    @Max(value = 20, message = "{round.result.place.Max}")
    private Long place;

    @NotNull(message = "{round.result.points.NotNull}")
    private Double points;

    @NotNull(message = "{round.result.user.NotNull}")
    private UserDto user;

    @NotNull(message = "{round.result.round.NotNull}")
    private RoundDto round;
}

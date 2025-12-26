package com.sgg.rounds.model;

import com.sgg.users.model.UserDto;
import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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
    private Integer place;

    private Double points;

    @NotNull(message = "{round.result.user.NotNull}")
    private UserDto user;
}

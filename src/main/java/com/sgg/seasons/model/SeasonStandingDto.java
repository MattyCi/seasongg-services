package com.sgg.seasons.model;

import com.sgg.users.UserDao;
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
public class SeasonStandingDto {

    private Long seasonStandingId;

    @NotNull
    @Min(0)
    @Max(999)
    private Integer place;

    @NotNull
    @Min(0)
    @Max(999)
    private Double points;

    @NotNull
    @Min(0)
    @Max(999)
    private int roundsPlayed;

    @NotNull
    private UserDto user;

    @NotNull
    private SeasonDto season;
}

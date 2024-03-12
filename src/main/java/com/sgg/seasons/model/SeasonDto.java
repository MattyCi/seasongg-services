package com.sgg.seasons.model;

import com.sgg.users.model.UserDto;
import lombok.*;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonDto {
    private Long seasonId;
    private String name;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private UserDto creator;
    private String status;

    // TODO: add rounds

    // TODO: add game

    // TODO: add season standings
}

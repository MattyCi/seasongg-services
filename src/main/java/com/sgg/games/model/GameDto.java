package com.sgg.games.model;

import com.sgg.seasons.model.SeasonDto;
import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Introspected
@Builder
public class GameDto {
    @NotNull(message = "{game.id.NotNull}")
    @PositiveOrZero(message = "{game.id.Positive}")
    private Long gameId;

    @NotNull(message = "{game.name.NotNull}")
    @Pattern(regexp = "^[\\p{L}\\p{N} :\\-'()!.,&\\[\"+*]+$", message = "{game.name.Pattern}")
    private String name;
}

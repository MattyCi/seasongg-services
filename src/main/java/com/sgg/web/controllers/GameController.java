package com.sgg.web.controllers;

import com.sgg.games.GameService;
import com.sgg.games.model.GameDto;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.validation.Validated;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.val;
import reactor.core.publisher.Mono;

import java.util.List;

@Validated
@Controller("${apiVersion}/games")
@AllArgsConstructor(onConstructor_ = @Inject)
public class GameController {
    private static final String SEARCH_QUERY_REGEX = "(?=.*[\\p{L}\\p{N}])[\\p{L}\\p{M}\\p{N} '’\\-:.,!?&()/$]+";
    private static final String SEARCH_QUERY_MESSAGE = "Invalid characters provided in search query.";
    GameService gameService;

    // TODO: eventually this should be only allowed by authenticated users
    @Get("/{gameId}")
    @Secured(SecurityRule.IS_ANONYMOUS)
    public Mono<GameDto> getGame(@PathVariable @Positive Long gameId) {
        return gameService.getGame(gameId);
    }

    // TODO: eventually this should be only allowed by authenticated users
    @Get("/popular")
    @Secured(SecurityRule.IS_ANONYMOUS)
    public HttpResponse<Mono<List<GameDto>>> getPopularGames() {
        val result = gameService.getPopularGames();
        return HttpResponse.status(HttpStatus.OK).body(result);
    }

    // TODO: eventually this should be only allowed by authenticated users
    @Get("/search")
    @Secured(SecurityRule.IS_ANONYMOUS)
    public HttpResponse<Mono<List<GameDto>>> searchGames(
            @QueryValue
            @NotBlank
            @Size(min = 2, max = 25)
            @Pattern(regexp = SEARCH_QUERY_REGEX, message = SEARCH_QUERY_MESSAGE)
            String query) {
        val result = gameService.searchGames(query);
        return HttpResponse.status(HttpStatus.OK).body(result);
    }
}
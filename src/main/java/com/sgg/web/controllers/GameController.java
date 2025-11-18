package com.sgg.web.controllers;

import com.sgg.games.ExternalGameClient;
import com.sgg.games.model.GameDto;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.val;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller("${apiVersion}/games")
@AllArgsConstructor(onConstructor_ = @Inject)
public class GameController {
    ExternalGameClient gameClient;

    // TODO: eventually this should be only allowed by authenticated users
    @Get("/popular")
    @Secured(SecurityRule.IS_ANONYMOUS)
    public HttpResponse<Mono<List<GameDto>>> getPopularGames() {
        val result = gameClient.getPopularGames();
        return HttpResponse.status(HttpStatus.OK).body(result);
    }
}

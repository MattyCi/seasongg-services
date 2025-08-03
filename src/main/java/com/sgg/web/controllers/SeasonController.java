package com.sgg.web.controllers;

import com.sgg.seasons.SeasonService;
import com.sgg.seasons.model.SeasonDto;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.val;

@Controller("${apiVersion}/seasons")
@AllArgsConstructor(onConstructor_ = @Inject)
public class SeasonController {

    SeasonService seasonService;

    @Get("/{id}")
    @Secured(SecurityRule.IS_ANONYMOUS)
    public HttpResponse<SeasonDto> getSeasonById(String id) {
        val result = seasonService.getSeason(id);
        return HttpResponse.status(HttpStatus.OK).body(result);
    }

    @Post
    @Secured(SecurityRule.IS_AUTHENTICATED)
    public HttpResponse<SeasonDto> createSeason(@Body SeasonDto season) {
        val result = seasonService.createSeason(season);
        return HttpResponse.status(HttpStatus.OK).body(result);
    }
}

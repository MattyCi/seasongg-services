package com.sgg.web.controllers;

import com.sgg.rounds.RoundService;
import com.sgg.rounds.model.RoundDto;
import com.sgg.seasons.SeasonService;
import com.sgg.seasons.model.SeasonDto;
import com.sgg.users.authz.SggSecurityRule;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.val;

import static com.sgg.users.authz.PermissionType.WRITE;
import static com.sgg.users.authz.ResourceType.SEASON;

@Controller("${apiVersion}/seasons")
@AllArgsConstructor(onConstructor_ = @Inject)
public class SeasonController {

    SeasonService seasonService;
    RoundService roundService;

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

    @Put("/{id}")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @SggSecurityRule(resourceType = SEASON, permissionType = WRITE, resourceIdName = "id")
    public HttpResponse<SeasonDto> updateSeason(@PathVariable String id, @Body SeasonDto season) {
        val result = seasonService.updateSeason(id, season);
        return HttpResponse.status(HttpStatus.OK).body(result);
    }

    @Delete("/{id}")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @SggSecurityRule(resourceType = SEASON, permissionType = WRITE, resourceIdName = "id")
    public HttpResponse<Void> deleteSeason(@PathVariable String id) {
        seasonService.deleteSeason(id);
        return HttpResponse.noContent();
    }

    @Get("/{id}/rounds/{roundId}")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    public HttpResponse<RoundDto> getRound(@PathVariable Long id, @PathVariable Long roundId) {
        val result = roundService.getRound(roundId);
        return HttpResponse.status(HttpStatus.OK).body(result);
    }

    @Post("/{id}/rounds")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @SggSecurityRule(resourceType = SEASON, permissionType = WRITE, resourceIdName = "id")
    public HttpResponse<SeasonDto> createRound(@PathVariable String id, @Body RoundDto round) {
        val result = roundService.addRound(id, round);
        return HttpResponse.status(HttpStatus.OK).body(result);
    }

    @Delete("/{id}/rounds/{roundId}")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @SggSecurityRule(resourceType = SEASON, permissionType = WRITE, resourceIdName = "id")
    public HttpResponse<RoundDto> deleteRound(@PathVariable String id, @PathVariable String roundId) {
        roundService.deleteRound(id, roundId);
        return HttpResponse.status(HttpStatus.OK);
    }
}

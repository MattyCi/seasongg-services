package com.sgg.games.bgg;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.uri.UriBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Knows how to build an authenticated request for each BGG XML API endpoint.
 * Nothing in this class knows or cares what the response looks like.
 */
@Singleton
public class BggRequestBuilder {

    private static final String BGG_HOST = "www.boardgamegeek.com";
    private static final String BOARD_GAME_TYPE = "boardgame";

    private final String bggToken;

    @Inject
    public BggRequestBuilder(@Value("${games.bgg.token}") String bggToken) {
        this.bggToken = bggToken;
    }

    public HttpRequest<?> gameById(long gameId) {
        return get("thing", Map.of("id", String.valueOf(gameId)));
    }

    public HttpRequest<?> gamesByIds(List<Long> gameIds) {
        String ids = gameIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        return get("thing", Map.of("id", ids));
    }

    public HttpRequest<?> popularGames() {
        return get("hot", Map.of("type", BOARD_GAME_TYPE));
    }

    public HttpRequest<?> searchGames(String query) {
        return get("search", Map.of("type", BOARD_GAME_TYPE, "query", query));
    }

    private HttpRequest<?> get(String path, Map<String, String> queryParams) {
        UriBuilder builder = UriBuilder.of("/xmlapi2")
                .scheme("https")
                .host(BGG_HOST)
                .path(path);
        queryParams.forEach(builder::queryParam);
        URI uri = builder.build();
        return HttpRequest.GET(uri).header("Authorization", "Bearer " + bggToken);
    }
}
package com.sgg.games;

import com.sgg.common.exception.NotFoundException;
import com.sgg.common.exception.SggException;
import com.sgg.games.bgg.BggGameXmlParser;
import com.sgg.games.bgg.BggRequestBuilder;
import com.sgg.games.model.GameDto;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Fetches game data from the BoardGameGeek XML API. Request construction
 * lives in {@link BggRequestBuilder}; response parsing lives in
 * {@link BggGameXmlParser}. This class is only responsible for orchestrating
 * the two of them over HTTP.
 */
@Singleton
@Slf4j
public class ExternalGameClient {

    private static final int MAX_THUMBNAIL_LOOKUP_IDS = 20;

    private final HttpClient httpClient;
    private final BggRequestBuilder requestBuilder;
    private final BggGameXmlParser xmlParser;

    @Inject
    public ExternalGameClient(HttpClient httpClient, BggRequestBuilder requestBuilder, BggGameXmlParser xmlParser) {
        this.httpClient = httpClient;
        this.requestBuilder = requestBuilder;
        this.xmlParser = xmlParser;
    }

    public Mono<GameDto> getGame(Long gameId) {
        return fetchAndParse(requestBuilder.gameById(gameId), xmlParser::parseSingleGame)
                .onErrorResume(this::handleGetGameError);
    }

    private Mono<GameDto> handleGetGameError(Throwable e) {
        if (e instanceof NotFoundException) {
            return Mono.empty();
        }
        log.error("Failed to retrieve or parse game from BGG", e);
        return Mono.error(new SggException("Unexpected error occurred finding game from BGG."));
    }

    public Mono<List<GameDto>> getPopularGames() {
        return fetchAndParse(requestBuilder.popularGames(), xmlParser::parsePopularGames)
                .onErrorResume(e -> logAndReturnEmptyList("popular games", e));
    }

    public Mono<List<GameDto>> searchGames(String query) {
        // TODO: validate query string before passing onto BGG
        return fetchAndParse(requestBuilder.searchGames(query), xmlParser::parseSearchResults)
                .flatMap(this::enrichWithThumbnails)
                .onErrorResume(e -> logAndReturnEmptyList("search results", e));
    }

    private Mono<List<GameDto>> enrichWithThumbnails(List<GameDto> games) {
        if (games.isEmpty()) {
            return Mono.just(games);
        }
        List<Long> idsToLookup = games.stream()
                .map(GameDto::getGameId)
                .limit(MAX_THUMBNAIL_LOOKUP_IDS)
                .collect(Collectors.toList());
        return fetchAndParse(requestBuilder.gamesByIds(idsToLookup), xmlParser::parseThumbnails)
                .map(thumbnailsById -> applyThumbnails(games, thumbnailsById))
                .onErrorResume(e -> {
                    log.error("Failed to enrich search results with thumbnails from BGG", e);
                    return Mono.just(games);
                });
    }

    private List<GameDto> applyThumbnails(List<GameDto> games, Map<Long, String> thumbnailsById) {
        games.forEach(game -> game.setThumbnail(thumbnailsById.get(game.getGameId())));
        return games;
    }

    private <T> Mono<List<T>> logAndReturnEmptyList(String what, Throwable e) {
        log.error("Failed to retrieve or parse {} from BGG", what, e);
        return Mono.just(List.of());
    }

    private <T> Mono<T> fetchAndParse(HttpRequest<?> request, Function<String, T> parse) {
        Publisher<String> publisher = httpClient.retrieve(request, String.class);
        return Mono.from(publisher)
                .flatMap(response -> Mono.fromCallable(() -> parse.apply(response))
                        .subscribeOn(Schedulers.boundedElastic()));
    }
}
package com.sgg.games;

import com.sgg.common.exception.SggException;
import com.sgg.games.model.GameDto;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.uri.UriBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lombok.val;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.reactivestreams.Publisher;
import org.xml.sax.SAXException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static org.w3c.dom.Node.ELEMENT_NODE;

@Singleton
@Slf4j
public final class ExternalGameClient {

    @Client
    private final HttpClient httpClient;
    private static final String BGG_HOST = "www.boardgamegeek.com";
    private static final URI POPULAR_GAMES_URI = UriBuilder.of("/xmlapi2")
            .scheme("https")
            .host(BGG_HOST)
            .path("hot")
            .queryParam("type", "boardgame")
            .build();

    @Value("${games.bgg.token}")
    private String bggToken;

    @Inject
    public ExternalGameClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Mono<List<GameDto>> getPopularGames() {
        HttpRequest<?> req = HttpRequest.GET(POPULAR_GAMES_URI)
                .header("Authorization", "Bearer " + bggToken);
        Publisher<String> publisher = httpClient.retrieve(req, String.class);
        return Mono.from(publisher)
                .flatMap(resp -> Mono.fromCallable(() -> parsePopularGamesXml(resp))
                        .subscribeOn(Schedulers.boundedElastic())
                )
                .onErrorResume(e -> {
                    log.error("Failed to retrieve or parse popular games from BGG", e);
                    return Mono.just(List.of());
                });
    }

    private List<GameDto> parsePopularGamesXml(String rawResponse) {
        List<GameDto> popularGames = new ArrayList<>();
        Document doc = getXmlDocument(rawResponse);
        NodeList items = doc.getElementsByTagName("item");
        if (items.getLength() == 0) {
            log.error("Zero items retrieved from BGG response.");
            return List.of();
        }
        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            String idAttr = item.getAttribute("id");
            String rankAttr = item.getAttribute("rank");
            long id;
            long rank;
            try {
                id = Long.parseLong(idAttr);
                rank = Long.parseLong(rankAttr);
            } catch (NumberFormatException nfe) {
                log.error("Unable to parse id/rank from BGG item {}, {}", idAttr, rankAttr);
                continue;
            }
            GameDto game = populateGameData(item);
            game.setGameId(id);
            game.setRank(rank);
            popularGames.add(game);
        }
        return popularGames;
    }

    private static Document getXmlDocument(String resp) {
        if (resp == null || resp.isBlank()) {
            val msg = "Response from BGG was empty.";
            log.error(msg);
            throw new SggException(msg);
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        Document doc;
        try {
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            db = dbf.newDocumentBuilder();
            doc = db.parse(new InputSource(new StringReader(resp)));
        } catch (SAXException | IOException | ParserConfigurationException e) {
            val msg = "Error parsing XML from BGG.";
            log.error(msg, e);
            throw new SggException(msg);
        }
        return doc;
    }

    private static GameDto populateGameData(Element item) {
        GameDto game = new GameDto();
        var childNodes = item.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            var childNode = childNodes.item(i);
            if (childNode.getNodeType() != ELEMENT_NODE) {
                log.debug("skipping non element child node...");
            } else if ("name".equals(childNode.getNodeName())) {
                game.setName(childNode.getAttributes().getNamedItem("value").getNodeValue());
            } else if ("yearpublished".equals(childNode.getNodeName())) {
                game.setYearPublished(childNode.getAttributes().getNamedItem("value").getNodeValue());
            } else if ("thumbnail".equals(childNode.getNodeName())) {
                game.setThumbnail(childNode.getAttributes().getNamedItem("value").getNodeValue());
            } else {
                log.warn("Unexpected node name when parsing child node from BGG XML: {}", childNode);
            }
        }
        return game;
    }
}

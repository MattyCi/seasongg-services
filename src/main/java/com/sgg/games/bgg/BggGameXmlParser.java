package com.sgg.games.bgg;

import com.sgg.common.exception.NotFoundException;
import com.sgg.common.exception.SggException;
import com.sgg.games.model.GameDto;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.w3c.dom.Node.ELEMENT_NODE;

/**
 * Turns raw BGG XML API responses into GameDto objects.
 */
@Singleton
@Slf4j
public class BggGameXmlParser {

    private static final String EL_ITEM = "item";
    private static final String EL_ERROR = "error";
    private static final String EL_NAME = "name";
    private static final String EL_YEAR_PUBLISHED = "yearpublished";
    private static final String EL_THUMBNAIL = "thumbnail";
    private static final String ATTR_ID = "id";
    private static final String ATTR_RANK = "rank";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_VALUE = "value";
    private static final String ATTR_MESSAGE = "message";
    private static final String PRIMARY_NAME_TYPE = "primary";

    public GameDto parseSingleGame(String rawResponse) {
        Document document = parseXml(rawResponse);
        assertNoBggError(document);
        Element item = singleItemOrThrow(document);
        GameDto game = toGame(item);
        game.setGameId(requireId(item));
        return game;
    }

    public List<GameDto> parsePopularGames(String rawResponse) {
        List<Element> items = itemElements(parseXml(rawResponse));
        if (items.isEmpty()) {
            log.error("Zero items retrieved from BGG response.");
            return List.of();
        }
        return items.stream()
                .map(this::parseRankedGame)
                .flatMap(Optional::stream)
                .toList();
    }

    public List<GameDto> parseSearchResults(String rawResponse) {
        List<Element> items = itemElements(parseXml(rawResponse));
        if (items.isEmpty()) {
            log.debug("Zero items retrieved from BGG search response.");
        }
        return items.stream()
                .map(this::parseGame)
                .flatMap(Optional::stream)
                .toList();
    }

    public Map<Long, String> parseThumbnails(String rawResponse) {
        Map<Long, String> thumbnailsById = new HashMap<>();
        for (Element item : itemElements(parseXml(rawResponse))) {
            parseId(item).ifPresent(id ->
                    findThumbnail(item).ifPresent(thumbnail -> thumbnailsById.put(id, thumbnail)));
        }
        return thumbnailsById;
    }

    private Optional<GameDto> parseGame(Element item) {
        return parseId(item).map(id -> {
            GameDto game = toGame(item);
            game.setGameId(id);
            return game;
        });
    }

    private Optional<GameDto> parseRankedGame(Element item) {
        Optional<Long> id = parseId(item);
        Optional<Long> rank = parseLong(item.getAttribute(ATTR_RANK), ATTR_RANK);
        if (id.isEmpty() || rank.isEmpty()) {
            return Optional.empty();
        }
        GameDto game = toGame(item);
        game.setGameId(id.get());
        game.setRank(rank.get());
        return Optional.of(game);
    }

    private GameDto toGame(Element item) {
        GameDto game = new GameDto();
        forEachElementChild(item, child -> applyField(game, child));
        return game;
    }

    private void applyField(GameDto game, Element field) {
        switch (field.getTagName()) {
            case EL_NAME -> applyPrimaryName(game, field);
            case EL_YEAR_PUBLISHED -> game.setYearPublished(BggXmlAttributes.read(field, ATTR_VALUE));
            case EL_THUMBNAIL -> game.setThumbnail(BggXmlAttributes.readThumbnail(field));
            default -> log.debug("Ignoring unrecognized BGG field: {}", field.getTagName());
        }
    }

    private void applyPrimaryName(GameDto game, Element nameElement) {
        String type = BggXmlAttributes.read(nameElement, ATTR_TYPE);
        boolean isPrimaryName = type == null || PRIMARY_NAME_TYPE.equals(type);
        if (isPrimaryName) {
            game.setName(BggXmlAttributes.read(nameElement, ATTR_VALUE));
        }
    }

    private Optional<String> findThumbnail(Element item) {
        NodeList thumbnails = item.getElementsByTagName(EL_THUMBNAIL);
        if (thumbnails.getLength() == 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(BggXmlAttributes.readThumbnail(thumbnails.item(0)));
    }

    private void assertNoBggError(Document document) {
        NodeList errors = document.getElementsByTagName(EL_ERROR);
        if (errors.getLength() > 0) {
            log.error(BggXmlAttributes.read(errors.item(0), ATTR_MESSAGE));
            throw new SggException("Error returned from BGG in get game call.");
        }
    }

    private Element singleItemOrThrow(Document document) {
        List<Element> items = itemElements(document);
        if (items.isEmpty()) {
            throw new NotFoundException("Game not found.");
        }
        if (items.size() > 1) {
            throw new SggException(String.format("BGG returned unexpected amount of games: %s", items.size()));
        }
        return items.get(0);
    }

    private long requireId(Element item) {
        return parseId(item).orElseThrow(() ->
                new SggException(String.format("Unable to parse id from BGG item %s", item.getAttribute(ATTR_ID))));
    }

    private Optional<Long> parseId(Element item) {
        return parseLong(item.getAttribute(ATTR_ID), ATTR_ID);
    }

    private Optional<Long> parseLong(String rawValue, String fieldName) {
        try {
            return Optional.of(Long.parseLong(rawValue));
        } catch (NumberFormatException e) {
            log.error("Unable to parse {} '{}' from BGG item", fieldName, rawValue);
            return Optional.empty();
        }
    }

    private List<Element> itemElements(Document document) {
        NodeList nodes = document.getElementsByTagName(EL_ITEM);
        List<Element> elements = new ArrayList<>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            elements.add((Element) nodes.item(i));
        }
        return elements;
    }

    private void forEachElementChild(Element parent, Consumer<Element> action) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == ELEMENT_NODE) {
                action.accept((Element) child);
            }
        }
    }

    private Document parseXml(String response) {
        if (response == null || response.isBlank()) {
            String message = "Response from BGG was empty.";
            log.error(message);
            throw new SggException(message);
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(response)));
        } catch (SAXException | IOException | ParserConfigurationException e) {
            String message = "Error parsing XML from BGG.";
            log.error(message, e);
            throw new SggException(message);
        }
    }
}
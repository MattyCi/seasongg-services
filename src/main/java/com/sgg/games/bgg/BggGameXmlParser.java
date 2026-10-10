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
import java.util.List;
import java.util.Optional;

/**
 * Serializes BGG XML API responses into GameDto objects.
 */
@Singleton
@Slf4j
public class BggGameXmlParser {

    private static final String ATTR_ID = "id";
    private static final String ATTR_RANK = "rank";
    private static final String ATTR_VALUE = "value";

    public GameDto parseSingleGame(String rawResponse) {
        Document document = parseXml(rawResponse);
        assertNoBggError(document);
        Element item = singleItemOrThrow(document);
        GameDto game = toGame(item);
        Long id = parseId(item).orElseThrow(() -> new SggException("BGG response missing game ID."));
        game.setGameId(id);
        return game;
    }

    private void assertNoBggError(Document document) {
        NodeList errors = document.getElementsByTagName("error");
        if (errors.getLength() > 0) {
            String errMsg = readAttrFromNode(errors.item(0), "message");
            log.error("Error message returned from BGG: {}", errMsg != null ? errMsg : "empty value");
            throw new SggException("Unexpected error returned from BGG.");
        }
    }

    private static String readAttrFromNode(Node node, String attributeName) {
        if (node == null || node.getAttributes() == null) {
            return null;
        }
        Node attribute = node.getAttributes().getNamedItem(attributeName);
        return attribute == null ? null : attribute.getNodeValue();
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

    private List<Element> itemElements(Document document) {
        NodeList nodes = document.getElementsByTagName("item");
        List<Element> elements = new ArrayList<>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            elements.add((Element) nodes.item(i));
        }
        return elements;
    }

    private GameDto toGame(Element item) {
        GameDto game = new GameDto();
        String name = null;
        String alternateName = null;

        NodeList children = item.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (!(children.item(i) instanceof Element field)) {
                continue; // whitespace, comments, etc.
            }
             switch (field.getTagName()) {
                case "name" -> {
                    String value = readAttrFromNode(field, ATTR_VALUE);
                    // BGG games can have a "primary" name and an "alternate" name
                    // use the primary name if it exists, otherwise use the alternate name
                    if ("alternate".equals(field.getAttribute("type"))) {
                        alternateName = alternateName != null ? alternateName : value;
                    } else {
                        name = name != null ? name : value;
                    }
                }
                case "yearpublished" -> game.setYearPublished(readAttrFromNode(field, ATTR_VALUE));
                case "thumbnail" -> game.setThumbnail(readThumbnailAttr(field));
                default -> log.debug("Ignoring unrecognized BGG field: {}", field.getTagName());
            }
        }
        game.setName(name != null ? name : alternateName);
        return game;
    }

    private static String readThumbnailAttr(Node thumbnailNode) {
        String attributeValue = readAttrFromNode(thumbnailNode, ATTR_VALUE);
        if (attributeValue != null) {
            return attributeValue;
        }
        Node textContent = thumbnailNode.getFirstChild();
        return textContent == null ? null : textContent.getNodeValue();
    }

    public List<GameDto> parsePopularGames(String rawResponse) {
        List<Element> items = itemElements(parseXml(rawResponse));
        if (items.isEmpty()) {
            log.error("Zero items retrieved from BGG response.");
            return List.of();
        }
        return items.stream()
                .map(this::parsePopularGame)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<GameDto> parsePopularGame(Element item) {
        Optional<Long> id = parseId(item);
        Optional<Long> rank = parseRank(item);
        if (id.isEmpty() || rank.isEmpty()) {
            return Optional.empty();
        }
        GameDto game = toGame(item);
        game.setGameId(id.get());
        game.setRank(rank.get());
        return Optional.of(game);
    }

    private Optional<Long> parseId(Element item) {
        return parseLong(item.getAttribute(ATTR_ID), ATTR_ID);
    }

    private Optional<Long> parseRank(Element item) {
        return parseLong(item.getAttribute(ATTR_RANK), ATTR_RANK);
    }

    private Optional<Long> parseLong(String rawValue, String fieldName) {
        try {
            return Optional.of(Long.parseLong(rawValue));
        } catch (NumberFormatException e) {
            log.error("Unable to parse {} '{}' from BGG item", fieldName, rawValue);
            return Optional.empty();
        }
    }

    public List<GameDto> parseSearchResults(String rawResponse) {
        List<Element> items = itemElements(parseXml(rawResponse));
        if (items.isEmpty()) {
            log.debug("Zero items retrieved from BGG search response.");
            return List.of();
        }
        return items.stream()
                .map(this::parseGame)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<GameDto> parseGame(Element item) {
        return parseId(item).map(id -> {
            GameDto game = toGame(item);
            game.setGameId(id);
            return game;
        });
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
            String message = "Error parsing response from BGG.";
            log.error(message, e);
            throw new SggException(message);
        }
    }
}
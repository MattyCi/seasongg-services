package com.sgg.games.bgg;

import org.w3c.dom.Node;

public final class BggXmlAttributes {

    /**
     * A thumbnail is expressed as a "value" attribute on some BGG endpoints,
     * and as the node's text content on others.
     */
    public static String readThumbnail(Node thumbnailNode) {
        String attributeValue = read(thumbnailNode, "value");
        if (attributeValue != null) {
            return attributeValue;
        }
        Node textContent = thumbnailNode.getFirstChild();
        return textContent == null ? null : textContent.getNodeValue();
    }

    public static String read(Node node, String attributeName) {
        if (node == null || node.getAttributes() == null) {
            return null;
        }
        Node attribute = node.getAttributes().getNamedItem(attributeName);
        return attribute == null ? null : attribute.getNodeValue();
    }
}
package com.sgg.games.bgg;

import org.w3c.dom.Node;

/**
 * BGG's XML responses represent most values as attributes, but sometimes as
 * element text content instead. This class hides that inconsistency and the
 * DOM API's null-happiness behind small, safe reads.
 */
public final class BggXmlAttributes {

    private BggXmlAttributes() {
        // utility class
    }

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
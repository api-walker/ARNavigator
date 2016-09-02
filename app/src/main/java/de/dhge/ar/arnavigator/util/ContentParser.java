package de.dhge.ar.arnavigator.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

// This library is for processing the content of a scanned QR Code
public class ContentParser {
    private Document doc;
    private Element metaNode;

    public ContentParser(String qrCodeContent) throws ParserConfigurationException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        ByteArrayInputStream input = new ByteArrayInputStream(qrCodeContent.getBytes("UTF-8"));

        try {
            this.doc = dBuilder.parse(input);
            doc.getDocumentElement().normalize();
            metaNode = (Element) doc.getElementsByTagName("Meta").item(0);
        } catch (SAXException e) {
           // Invalid content
        }
    }

    /**
     * Get type of object
     *
     * @return String type
     */
    public String getType() {
        return getCustomNode("type");
    }

    /**
     * Get name of object
     *
     * @return String name
     */
    public String getName() {
        return getCustomNode("name");
    }

    /**
     * Get type of object
     *
     * @return String type
     */
    public String getID() {
        return getCustomNode("id");
    }

    /**
     * Get type of content of object (raw?)
     *
     * @return Boolean isRaw
     */
    public boolean isRawContent() {
        Node attr = metaNode.getElementsByTagName("content").item(0).getAttributes().getNamedItem("raw");
        return attr != null && attr.getNodeValue().equals("true");
    }

    /**
     * Get type of content of object (webcontent?)
     *
     * @return Boolean isWebContent
     */
    public boolean isWebContent() {
        Node attr = metaNode.getElementsByTagName("content").item(0).getAttributes().getNamedItem("web_content");
        return attr != null && attr.getNodeValue().equals("true");
    }

    /**
     * Check if QR-Code is with ARNavigator content
     *
     * @return Boolean isValid
     */
    public boolean isValidContent() {
        return (metaNode != null);
    }

    /**
     * Get content of object (shown in ARPopup)
     *
     * @return String content
     */
    public String getContent() {
        return getCustomNode("content");
    }

    /**
     * Get custom meta tag
     *
     * @param name Name of custom tag
     * @return String content of tag
     */
    public String getCustomNode(String name) {
        return metaNode.getElementsByTagName(name).item(0).getTextContent();
    }
}
